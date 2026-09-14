/**
 * @file SqliteStorageProperties
 * @project SlothNote
 * @module SQLite storage configuration
 * @description Declares the optional SQLite database path and connection-pool tuning values.
 * @logic Binds external configuration and leaves path resolution to SqliteDataSourceConfig.
 * @dependencies Spring Boot ConfigurationProperties
 * @index_tags SQLite, database, configuration, storage
 * @author holic512
 */
package org.example.backend.common.config.database;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage.sqlite")
public class SqliteStorageProperties {

    /** Optional absolute or working-directory-relative SQLite database file path. */
    private String path;

    /** Time SQLite waits for a writer lock before failing a request. */
    private int busyTimeoutMs = 5000;

    /** SQLite permits one writer at a time; keep the default pool deliberately small. */
    private int maximumPoolSize = 4;
}
