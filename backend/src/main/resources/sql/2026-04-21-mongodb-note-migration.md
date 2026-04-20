# MongoDB 笔记正文迁移说明

本次迁移目标：将 MongoDB `notes` 集合中的正文数据迁入 MySQL `note_content` 表。

迁移组件：
- `org.example.backend.tool.NoteContentMongoMigrationRunner`

建议执行顺序：
1. 先执行 `2026-04-21-note-content-mysql.sql`
2. 使用 JVM 参数或环境变量提供 Mongo 连接信息并开启迁移开关
3. 观察日志中的 `total/success/skipped/failed`
4. 抽样核对 `note_id/content/last_saved_at`
5. 校验正文读取、搜索、版本恢复后再切换正式版本

启用示例：

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--note-content.migration.enabled=true --note-content.migration.mongo-uri=mongodb://admin:123456@localhost:27017/slothnote?authSource=admin"
```

迁移规则：
- 只迁移 `note_info` 中存在的笔记
- MySQL 已有正文时，以 Mongo 数据覆盖
- Mongo 中正文为空时按空字符串写入，保持结构完整
- `lastSavedAt` 为空时允许写入 `NULL`
