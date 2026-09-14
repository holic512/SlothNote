/**
 * @file SqliteDataSourceConfig
 * @project SlothNote
 * @module Database configuration
 * @description Creates the application's SQLite-backed pooled data source and its private database directory.
 * @logic Resolves the configured database path, creates its parent directory, then applies SQLite durability and foreign-key settings to every pooled connection.
 * @dependencies SQLite JDBC, HikariCP, LocalStorageProperties, SqliteStorageProperties
 * @index_tags SQLite, datasource, WAL, foreign keys, local storage
 * @author holic512
 */
package org.example.backend.common.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.backend.common.util.file.LocalStorageProperties;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

@Configuration
public class SqliteDataSourceConfig {

    private static final String DEFAULT_DATABASE_FILE = "base/slothnote.db";

    private final LocalStorageProperties localStorageProperties;
    private final SqliteStorageProperties sqliteStorageProperties;

    public SqliteDataSourceConfig(LocalStorageProperties localStorageProperties,
                                  SqliteStorageProperties sqliteStorageProperties) {
        this.localStorageProperties = localStorageProperties;
        this.sqliteStorageProperties = sqliteStorageProperties;
    }

    @Bean(destroyMethod = "close")
    @Primary
    public HikariDataSource dataSource() {
        Path databasePath = resolveDatabasePath();
        createParentDirectory(databasePath);

        SQLiteConfig sqliteConfig = new SQLiteConfig();
        sqliteConfig.enforceForeignKeys(true);
        sqliteConfig.setJournalMode(SQLiteConfig.JournalMode.WAL);
        sqliteConfig.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        sqliteConfig.setDateClass("TEXT");
        sqliteConfig.setDateStringFormat("yyyy-MM-dd HH:mm:ss");
        sqliteConfig.setBusyTimeout(Math.max(0, sqliteStorageProperties.getBusyTimeoutMs()));

        SQLiteDataSource sqliteDataSource = new SQLiteDataSource(sqliteConfig);
        sqliteDataSource.setUrl("jdbc:sqlite:" + databasePath);

        HikariConfig poolConfig = new HikariConfig();
        poolConfig.setPoolName("slothnote-sqlite");
        poolConfig.setDataSource(sqliteDataSource);
        poolConfig.setMaximumPoolSize(Math.max(1, sqliteStorageProperties.getMaximumPoolSize()));
        poolConfig.setMinimumIdle(1);
        poolConfig.setConnectionTimeout(Math.max(1000L, sqliteStorageProperties.getBusyTimeoutMs()));
        return new HikariDataSource(poolConfig);
    }

    private Path resolveDatabasePath() {
        String configuredPath = sqliteStorageProperties.getPath();
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Path.of(configuredPath.trim()).toAbsolutePath().normalize();
        }

        String rootDirectory = localStorageProperties.getRootDir();
        if (rootDirectory == null || rootDirectory.isBlank()) {
            throw new IllegalStateException("storage.local.root-dir must be configured for SQLite storage");
        }
        return Path.of(rootDirectory.trim())
                .resolve(DEFAULT_DATABASE_FILE)
                .toAbsolutePath()
                .normalize();
    }

    private void createParentDirectory(Path databasePath) {
        Path parent = databasePath.getParent();
        if (parent == null) {
            throw new IllegalStateException("SQLite database path must include a parent directory");
        }
        try {
            java.nio.file.Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to create SQLite database directory: " + parent, exception);
        }
    }
}
