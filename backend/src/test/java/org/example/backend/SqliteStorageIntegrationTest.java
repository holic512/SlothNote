/**
 * @file SqliteStorageIntegrationTest
 * @project SlothNote
 * @module SQLite integration test
 * @description Verifies first-start SQLite initialization, persistence idempotence, data access, reset behavior, and private database-file routing.
 * @logic Starts the complete application against a temporary local-storage root and exercises its public persistence infrastructure.
 * @dependencies Spring Boot Test, JdbcTemplate, JPA, MyBatis, MockMvc
 * @index_tags SQLite, integration test, schema, static files, initialization
 * @author holic512
 */
package org.example.backend;

import org.example.backend.admin.setting.service.AdminSettingService;
import org.example.backend.common.entity.Admin;
import org.example.backend.common.mapper.NoteMapper;
import org.example.backend.common.repository.AdminRepository;
import org.example.backend.common.util.StpKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SqliteStorageIntegrationTest {

    private static final Path STORAGE_ROOT = Path.of(System.getProperty("java.io.tmpdir"), "slothnote-sqlite-it-" + UUID.randomUUID());

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminSettingService adminSettingService;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.local.root-dir", STORAGE_ROOT::toString);
        registry.add("storage.sqlite.path", () -> "");
    }

    @Test
    void initializesAnIdempotentPrivateSqliteStoreAndSupportsPersistence() throws Exception {
        Path database = STORAGE_ROOT.resolve("base/slothnote.db");
        assertThat(database).exists();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'", Integer.class
        )).isEqualTo(20);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'user_ai_permissions'", Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE type = 'trigger'", Integer.class))
                .isEqualTo(18);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'trigger' AND name = 'trg_user_ai_permissions_updated_at'", Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("PRAGMA foreign_keys", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("PRAGMA journal_mode", String.class)).isEqualTo("wal");
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_ai_config", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_mail_config", Integer.class)).isEqualTo(1);

        mockMvc.perform(get("/admin/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.needInit").value(true));
        mockMvc.perform(post("/admin/auth/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bootstrap-admin\",\"password\":\"bootstrap-password\",\"email\":\"bootstrap@example.test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
        mockMvc.perform(get("/admin/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.needInit").value(false));
        mockMvc.perform(post("/admin/auth/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bootstrap-admin\",\"password\":\"bootstrap-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(409));

        Path avatarDirectory = STORAGE_ROOT.resolve("avatar");
        Files.createDirectories(avatarDirectory);
        Files.writeString(avatarDirectory.resolve("visible.txt"), "visible avatar");
        mockMvc.perform(get("/files/avatar/visible.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("visible avatar"));
        mockMvc.perform(get("/files/base/slothnote.db"))
                .andExpect(status().isNotFound());

        Admin admin = new Admin();
        admin.setUsername("sqlite-admin");
        admin.setPassword("hashed-password");
        admin.setEmail("sqlite-admin@example.test");
        adminRepository.save(admin);
        assertThat(admin.getId()).isNotNull();
        assertThat(adminRepository.findByUsername("sqlite-admin").getCreatedAt()).isNotNull();

        jdbcTemplate.update(
                "INSERT INTO admins (username, password, is_deleted, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                "legacy-time-admin", "hashed-password", 0, "1789375155480", "1789375155480"
        );
        ResourceDatabasePopulator timestampMigration = new ResourceDatabasePopulator(
                new ClassPathResource("sql/sqlite/migrate-legacy-timestamps.sql")
        );
        timestampMigration.setSeparator("@@");
        timestampMigration.execute(dataSource);
        assertThat(jdbcTemplate.queryForObject("SELECT created_at FROM admins WHERE username = ?", String.class, "legacy-time-admin"))
                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        assertThat(adminRepository.findByUsername("legacy-time-admin").getCreatedAt()).isNotNull();

        jdbcTemplate.update("INSERT INTO users (uid, username, password, email, status) VALUES (?, ?, ?, ?, ?)",
                "sqlite-user", "sqlite-user", "hashed-password", "sqlite-user@example.test", 0);
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE uid = ?", Long.class, "sqlite-user");
        ResourceDatabasePopulator aiPermissionMigration = new ResourceDatabasePopulator(
                new ClassPathResource("sql/sqlite/migrate-user-ai-permissions.sql")
        );
        aiPermissionMigration.setSeparator("@@");
        aiPermissionMigration.execute(dataSource);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_ai_permissions WHERE user_id = ?", Integer.class, userId
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT can_read_all_notes FROM user_ai_permissions WHERE user_id = ?", Integer.class, userId
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT can_write_note_content FROM user_ai_permissions WHERE user_id = ?", Integer.class, userId
        )).isZero();
        jdbcTemplate.update("INSERT INTO note_info (user_id, note_title, note_type) VALUES (?, ?, ?)", userId, "SQLite note", 0);
        Long noteId = jdbcTemplate.queryForObject("SELECT id FROM note_info WHERE user_id = ?", Long.class, userId);
        jdbcTemplate.update("INSERT INTO note_content (note_id, content) VALUES (?, ?)", noteId, "A searchable SQLite needle");
        assertThat(noteMapper.findByContentLike("needle"))
                .extracting(note -> note.getNoteId())
                .containsExactly(noteId);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO folder_info (user_id, folder_name) VALUES (?, ?)", 999999L, "invalid foreign key"))
                .isInstanceOf(DataAccessException.class);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("sql/sqlite/schema.sql"),
                new ClassPathResource("sql/sqlite/data.sql"),
                new ClassPathResource("sql/sqlite/migrate-legacy-timestamps.sql"),
                new ClassPathResource("sql/sqlite/migrate-user-ai-permissions.sql")
        );
        populator.setSeparator("@@");
        populator.execute(dataSource);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_ai_config", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM system_mail_config", Integer.class)).isEqualTo(1);

        StpKit.ADMIN.login(admin.getId());
        try {
            adminSettingService.resetUserData("INITIALIZE");
        } finally {
            StpKit.ADMIN.logout();
        }
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class)).isZero();
        jdbcTemplate.update("INSERT INTO users (uid, username, password, email, status) VALUES (?, ?, ?, ?, ?)",
                "post-reset-user", "post-reset-user", "hashed-password", "post-reset-user@example.test", 0);
        assertThat(jdbcTemplate.queryForObject("SELECT id FROM users WHERE uid = ?", Long.class, "post-reset-user"))
                .isEqualTo(1L);
    }

    @AfterAll
    static void deleteTemporaryStorage() throws IOException {
        if (!Files.exists(STORAGE_ROOT)) {
            return;
        }
        try (var paths = Files.walk(STORAGE_ROOT)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Unable to delete test path: " + path, exception);
                }
            });
        }
    }
}
