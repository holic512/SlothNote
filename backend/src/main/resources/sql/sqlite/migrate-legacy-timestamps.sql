-- SQLite JDBC previously persisted LocalDateTime values as epoch milliseconds in this project.
-- Convert digit-only legacy values to local SQLite date-time text. Columns covered by an
-- updated_at trigger are converted in one statement so the trigger cannot replace the
-- original update time while a sibling timestamp is being migrated.

UPDATE system_ai_config
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE system_mail_config
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE users
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE admins
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE folder_info
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE note_info
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE note_content
SET last_saved_at = CASE WHEN last_saved_at IS NOT NULL AND (typeof(last_saved_at) = 'integer' OR (length(trim(last_saved_at)) >= 11 AND trim(last_saved_at) NOT GLOB '*[^0-9]*')) THEN datetime(CAST(trim(last_saved_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE last_saved_at END,
    created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE (last_saved_at IS NOT NULL AND (typeof(last_saved_at) = 'integer' OR (length(trim(last_saved_at)) >= 11 AND trim(last_saved_at) NOT GLOB '*[^0-9]*'))) OR typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE note_reference
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE favorite_folder_info
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE favorite_note_info
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE todo_category
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE todo_info
SET start_date = CASE WHEN start_date IS NOT NULL AND (typeof(start_date) = 'integer' OR (length(trim(start_date)) >= 11 AND trim(start_date) NOT GLOB '*[^0-9]*')) THEN datetime(CAST(trim(start_date) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE start_date END,
    due_date = CASE WHEN due_date IS NOT NULL AND (typeof(due_date) = 'integer' OR (length(trim(due_date)) >= 11 AND trim(due_date) NOT GLOB '*[^0-9]*')) THEN datetime(CAST(trim(due_date) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE due_date END,
    created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE (start_date IS NOT NULL AND (typeof(start_date) = 'integer' OR (length(trim(start_date)) >= 11 AND trim(start_date) NOT GLOB '*[^0-9]*'))) OR (due_date IS NOT NULL AND (typeof(due_date) = 'integer' OR (length(trim(due_date)) >= 11 AND trim(due_date) NOT GLOB '*[^0-9]*'))) OR typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE user_profiles
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE comments
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE note_version
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END
WHERE typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*')@@

UPDATE auth_ticket
SET expire_at = CASE WHEN typeof(expire_at) = 'integer' OR (length(trim(expire_at)) >= 11 AND trim(expire_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(expire_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE expire_at END,
    used_at = CASE WHEN used_at IS NOT NULL AND (typeof(used_at) = 'integer' OR (length(trim(used_at)) >= 11 AND trim(used_at) NOT GLOB '*[^0-9]*')) THEN datetime(CAST(trim(used_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE used_at END,
    created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(expire_at) = 'integer' OR typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (used_at IS NOT NULL AND typeof(used_at) = 'integer') OR (length(trim(expire_at)) >= 11 AND trim(expire_at) NOT GLOB '*[^0-9]*') OR (used_at IS NOT NULL AND length(trim(used_at)) >= 11 AND trim(used_at) NOT GLOB '*[^0-9]*') OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE ai_chat_session
SET last_message_at = CASE WHEN typeof(last_message_at) = 'integer' OR (length(trim(last_message_at)) >= 11 AND trim(last_message_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(last_message_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE last_message_at END,
    created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(last_message_at) = 'integer' OR typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(last_message_at)) >= 11 AND trim(last_message_at) NOT GLOB '*[^0-9]*') OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE ai_chat_message
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END,
    updated_at = CASE WHEN typeof(updated_at) = 'integer' OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(updated_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE updated_at END
WHERE typeof(created_at) = 'integer' OR typeof(updated_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') OR (length(trim(updated_at)) >= 11 AND trim(updated_at) NOT GLOB '*[^0-9]*')@@

UPDATE ai_chat_session_note_ref
SET created_at = CASE WHEN typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*') THEN datetime(CAST(trim(created_at) AS INTEGER) / 1000, 'unixepoch', 'localtime') ELSE created_at END
WHERE typeof(created_at) = 'integer' OR (length(trim(created_at)) >= 11 AND trim(created_at) NOT GLOB '*[^0-9]*')@@
