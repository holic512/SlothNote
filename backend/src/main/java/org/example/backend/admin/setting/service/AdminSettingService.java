package org.example.backend.admin.setting.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminSettingService {

    private static final String RESET_CONFIRM_TEXT = "INITIALIZE";

    private final JdbcTemplate jdbcTemplate;

    public AdminSettingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getSystemResetSummary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("confirmText", RESET_CONFIRM_TEXT);
        data.put("tables", buildTableCounts());
        return data;
    }

    @Transactional
    public Map<String, Object> resetUserData(String confirmText) {
        if (!RESET_CONFIRM_TEXT.equals(confirmText)) {
            throw new IllegalArgumentException("确认词不匹配");
        }

        List<String> resetTables = new ArrayList<>();
        for (String table : resetOrder()) {
            if (!tableExists(table)) {
                continue;
            }
            jdbcTemplate.update("DELETE FROM " + table);
            resetTables.add(table);
            if (supportsAutoIncrement(table)) {
                jdbcTemplate.execute("ALTER TABLE " + table + " AUTO_INCREMENT = 1");
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("confirmText", RESET_CONFIRM_TEXT);
        data.put("resetTables", resetTables);
        data.put("tables", buildTableCounts());
        return data;
    }

    private List<Map<String, Object>> buildTableCounts() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String table : summaryOrder()) {
            if (!tableExists(table)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tableName", table);
            row.put("count", countTable(table));
            rows.add(row);
        }
        return rows;
    }

    private Integer countTable(String table) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return count == null ? 0 : count;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean supportsAutoIncrement(String table) {
        return !"note_content".equals(table);
    }

    private List<String> summaryOrder() {
        return List.of(
                "users",
                "user_profiles",
                "folder_info",
                "note_info",
                "note_content",
                "note_version",
                "comments",
                "favorite_folder_info",
                "favorite_note_info",
                "todo_category",
                "todo_info",
                "ai_chat_session",
                "ai_chat_message",
                "ai_chat_session_note_ref",
                "auth_ticket"
        );
    }

    private List<String> resetOrder() {
        return List.of(
                "ai_chat_session_note_ref",
                "ai_chat_message",
                "comments",
                "note_version",
                "note_content",
                "favorite_note_info",
                "favorite_folder_info",
                "note_info",
                "folder_info",
                "todo_info",
                "todo_category",
                "user_profiles",
                "ai_chat_session",
                "auth_ticket",
                "users"
        );
    }
}
