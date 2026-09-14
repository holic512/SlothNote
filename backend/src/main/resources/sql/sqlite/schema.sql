CREATE TABLE IF NOT EXISTS system_ai_config (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    provider_name TEXT NOT NULL DEFAULT 'openai-compatible',
    base_url TEXT,
    api_key TEXT,
    model TEXT,
    temperature REAL NOT NULL DEFAULT 0.7,
    max_tokens INTEGER NOT NULL DEFAULT 4096,
    planner_temperature REAL NOT NULL DEFAULT 0.1,
    planner_max_tokens INTEGER NOT NULL DEFAULT 256,
    enabled INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
)@@

CREATE TABLE IF NOT EXISTS system_mail_config (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    host TEXT,
    port INTEGER NOT NULL DEFAULT 465,
    username TEXT,
    password TEXT,
    protocol TEXT NOT NULL DEFAULT 'smtps',
    default_encoding TEXT NOT NULL DEFAULT 'UTF-8',
    from_address TEXT,
    from_name TEXT,
    smtp_auth INTEGER NOT NULL DEFAULT 1 CHECK (smtp_auth IN (0, 1)),
    ssl_enable INTEGER NOT NULL DEFAULT 1 CHECK (ssl_enable IN (0, 1)),
    starttls_enable INTEGER NOT NULL DEFAULT 1 CHECK (starttls_enable IN (0, 1)),
    starttls_required INTEGER NOT NULL DEFAULT 1 CHECK (starttls_required IN (0, 1)),
    enabled INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
)@@

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uid TEXT NOT NULL,
    username TEXT NOT NULL,
    password TEXT NOT NULL,
    email TEXT NOT NULL,
    status INTEGER NOT NULL,
    has_profile INTEGER,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (uid),
    UNIQUE (username),
    UNIQUE (email)
)@@
CREATE INDEX IF NOT EXISTS idx_users_status_is_deleted ON users (status, is_deleted)@@
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at)@@

CREATE TABLE IF NOT EXISTS admins (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    password TEXT NOT NULL,
    email TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (username),
    UNIQUE (email)
)@@
CREATE INDEX IF NOT EXISTS idx_admins_created_at ON admins (created_at)@@
CREATE INDEX IF NOT EXISTS idx_admins_is_deleted ON admins (is_deleted)@@

CREATE TABLE IF NOT EXISTS folder_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    folder_name TEXT NOT NULL,
    parent_id INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    folder_avatar TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_folder_user_id ON folder_info (user_id)@@
CREATE INDEX IF NOT EXISTS idx_folder_parent_id ON folder_info (parent_id)@@
CREATE INDEX IF NOT EXISTS idx_folder_user_deleted ON folder_info (user_id, is_deleted)@@
CREATE INDEX IF NOT EXISTS idx_folder_parent_deleted ON folder_info (parent_id, is_deleted)@@

CREATE TABLE IF NOT EXISTS note_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    folder_id INTEGER,
    note_title TEXT,
    note_summary TEXT,
    note_avatar TEXT,
    note_cover_url TEXT,
    note_password TEXT,
    note_type INTEGER NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folder_info (id) ON DELETE SET NULL ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_note_user_id ON note_info (user_id)@@
CREATE INDEX IF NOT EXISTS idx_note_folder_id ON note_info (folder_id)@@
CREATE INDEX IF NOT EXISTS idx_note_user_deleted ON note_info (user_id, is_deleted)@@
CREATE INDEX IF NOT EXISTS idx_note_folder_deleted ON note_info (folder_id, is_deleted)@@
CREATE INDEX IF NOT EXISTS idx_note_type ON note_info (note_type)@@

CREATE TABLE IF NOT EXISTS note_content (
    note_id INTEGER PRIMARY KEY,
    content TEXT,
    last_saved_at TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (note_id) REFERENCES note_info (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@

CREATE TABLE IF NOT EXISTS note_reference (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    source_note_id INTEGER NOT NULL,
    target_note_id INTEGER NOT NULL,
    label_snapshot TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (source_note_id, target_note_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (source_note_id) REFERENCES note_info (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (target_note_id) REFERENCES note_info (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_note_reference_user_id ON note_reference (user_id)@@
CREATE INDEX IF NOT EXISTS idx_note_reference_source ON note_reference (source_note_id)@@
CREATE INDEX IF NOT EXISTS idx_note_reference_target ON note_reference (target_note_id)@@

CREATE TABLE IF NOT EXISTS favorite_folder_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    folder_name TEXT NOT NULL,
    parent_id INTEGER NOT NULL DEFAULT 0,
    favorite_folder_description TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_fav_folder_user_id ON favorite_folder_info (user_id)@@
CREATE INDEX IF NOT EXISTS idx_fav_folder_parent_id ON favorite_folder_info (parent_id)@@
CREATE INDEX IF NOT EXISTS idx_fav_folder_user_deleted ON favorite_folder_info (user_id, is_deleted)@@

CREATE TABLE IF NOT EXISTS favorite_note_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    note_id INTEGER NOT NULL,
    favorite_folder_id INTEGER NOT NULL DEFAULT 0,
    favorite_status INTEGER NOT NULL DEFAULT 1,
    note_remark TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (note_id) REFERENCES note_info (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_fav_note_user_id ON favorite_note_info (user_id)@@
CREATE INDEX IF NOT EXISTS idx_fav_note_note_id ON favorite_note_info (note_id)@@
CREATE INDEX IF NOT EXISTS idx_fav_note_folder_id ON favorite_note_info (favorite_folder_id)@@
CREATE INDEX IF NOT EXISTS idx_fav_note_user_status ON favorite_note_info (user_id, favorite_status)@@
CREATE INDEX IF NOT EXISTS idx_fav_note_user_deleted ON favorite_note_info (user_id, is_deleted)@@

CREATE TABLE IF NOT EXISTS todo_category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    type INTEGER NOT NULL,
    name TEXT NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_todo_category_user_id ON todo_category (user_id)@@
CREATE INDEX IF NOT EXISTS idx_todo_category_user_deleted_type ON todo_category (user_id, is_deleted, type)@@

CREATE TABLE IF NOT EXISTS todo_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    category_id INTEGER,
    title TEXT,
    description TEXT,
    start_date TEXT,
    due_date TEXT,
    status INTEGER DEFAULT 0,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (category_id) REFERENCES todo_category (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_todo_info_user_id ON todo_info (user_id)@@
CREATE INDEX IF NOT EXISTS idx_todo_info_category_id ON todo_info (category_id)@@
CREATE INDEX IF NOT EXISTS idx_todo_info_user_deleted_status ON todo_info (user_id, is_deleted, status)@@
CREATE INDEX IF NOT EXISTS idx_todo_info_category_deleted ON todo_info (category_id, is_deleted)@@

CREATE TABLE IF NOT EXISTS user_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    nickname TEXT NOT NULL,
    gender TEXT NOT NULL,
    age INTEGER,
    bio TEXT,
    contact_info TEXT,
    avatar TEXT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_user_profiles_nickname ON user_profiles (nickname)@@
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_deleted ON user_profiles (user_id, is_deleted)@@

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

CREATE TABLE IF NOT EXISTS comments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    parent_id INTEGER,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (note_id) REFERENCES note_info (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE SET NULL ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_comments_note_id ON comments (note_id)@@
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments (user_id)@@
CREATE INDEX IF NOT EXISTS idx_comments_parent_id ON comments (parent_id)@@
CREATE INDEX IF NOT EXISTS idx_comments_note_deleted ON comments (note_id, is_deleted)@@

CREATE TABLE IF NOT EXISTS note_version (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    note_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    version_no INTEGER NOT NULL,
    content_json TEXT NOT NULL,
    content_preview TEXT,
    source_type TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (note_id, version_no),
    FOREIGN KEY (note_id) REFERENCES note_info (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_note_version_note_user ON note_version (note_id, user_id, created_at)@@

CREATE TABLE IF NOT EXISTS auth_ticket (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_id TEXT NOT NULL,
    ticket_type TEXT NOT NULL,
    subject_key TEXT NOT NULL,
    code TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    expire_at TEXT NOT NULL,
    used_at TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (ticket_id)
)@@
CREATE INDEX IF NOT EXISTS idx_auth_ticket_type_subject ON auth_ticket (ticket_type, subject_key)@@
CREATE INDEX IF NOT EXISTS idx_auth_ticket_expire_at ON auth_ticket (expire_at)@@

CREATE TABLE IF NOT EXISTS ai_chat_session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    last_message_at TEXT NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_ai_chat_session_user ON ai_chat_session (user_id, is_deleted, last_message_at)@@

CREATE TABLE IF NOT EXISTS ai_chat_message (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    role TEXT NOT NULL,
    message_type TEXT NOT NULL,
    content_md TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES ai_chat_session (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_ai_chat_message_session ON ai_chat_message (session_id, created_at)@@
CREATE INDEX IF NOT EXISTS idx_ai_chat_message_user ON ai_chat_message (user_id, created_at)@@

CREATE TABLE IF NOT EXISTS ai_chat_session_note_ref (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    note_id INTEGER NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (session_id, note_id),
    FOREIGN KEY (session_id) REFERENCES ai_chat_session (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (note_id) REFERENCES note_info (id) ON DELETE CASCADE ON UPDATE CASCADE
)@@
CREATE INDEX IF NOT EXISTS idx_ai_chat_session_note_ref_user ON ai_chat_session_note_ref (user_id, session_id)@@

CREATE TRIGGER IF NOT EXISTS trg_system_ai_config_updated_at
AFTER UPDATE ON system_ai_config FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE system_ai_config SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_system_mail_config_updated_at
AFTER UPDATE ON system_mail_config FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE system_mail_config SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_users_updated_at
AFTER UPDATE ON users FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_admins_updated_at
AFTER UPDATE ON admins FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE admins SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_folder_info_updated_at
AFTER UPDATE ON folder_info FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE folder_info SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_note_info_updated_at
AFTER UPDATE ON note_info FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE note_info SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_note_content_updated_at
AFTER UPDATE ON note_content FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE note_content SET updated_at = CURRENT_TIMESTAMP WHERE note_id = NEW.note_id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_note_reference_updated_at
AFTER UPDATE ON note_reference FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE note_reference SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_favorite_folder_info_updated_at
AFTER UPDATE ON favorite_folder_info FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE favorite_folder_info SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_favorite_note_info_updated_at
AFTER UPDATE ON favorite_note_info FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE favorite_note_info SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_todo_category_updated_at
AFTER UPDATE ON todo_category FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE todo_category SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_todo_info_updated_at
AFTER UPDATE ON todo_info FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE todo_info SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_user_profiles_updated_at
AFTER UPDATE ON user_profiles FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE user_profiles SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_user_ai_permissions_updated_at
AFTER UPDATE ON user_ai_permissions FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE user_ai_permissions SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_comments_updated_at
AFTER UPDATE ON comments FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE comments SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_auth_ticket_updated_at
AFTER UPDATE ON auth_ticket FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE auth_ticket SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_ai_chat_session_updated_at
AFTER UPDATE ON ai_chat_session FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE ai_chat_session SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@

CREATE TRIGGER IF NOT EXISTS trg_ai_chat_message_updated_at
AFTER UPDATE ON ai_chat_message FOR EACH ROW WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE ai_chat_message SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END@@
