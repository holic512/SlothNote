-- 用户 AI 权限：作用于当前用户旗下的全部笔记，不按单篇笔记保存。
CREATE TABLE IF NOT EXISTS user_ai_permissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    can_read_all_notes INTEGER NOT NULL DEFAULT 1,
    can_write_note_content INTEGER NOT NULL DEFAULT 0,
    can_write_note_title INTEGER NOT NULL DEFAULT 0,
    can_write_note_summary INTEGER NOT NULL DEFAULT 0,
    can_write_note_cover INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_user_ai_permissions_user ON user_ai_permissions (user_id)@@
CREATE TRIGGER IF NOT EXISTS trg_user_ai_permissions_updated_at
AFTER UPDATE ON user_ai_permissions FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE user_ai_permissions SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

INSERT OR IGNORE INTO user_ai_permissions (user_id)
SELECT id FROM users@@
