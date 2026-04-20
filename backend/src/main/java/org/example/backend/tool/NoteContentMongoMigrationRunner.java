package org.example.backend.tool;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.example.backend.common.domain.Note;
import org.example.backend.user.note.note.repository.UNoteInfoRep;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class NoteContentMongoMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NoteContentMongoMigrationRunner.class);

    private final UNoteInfoRep noteInfoRep;
    private final UNoteRepM noteRep;

    @Value("${note-content.migration.enabled:false}")
    private boolean enabled;

    @Value("${note-content.migration.mongo-uri:}")
    private String mongoUri;

    @Value("${note-content.migration.database:slothnote}")
    private String database;

    @Value("${note-content.migration.collection:notes}")
    private String collection;

    public NoteContentMongoMigrationRunner(UNoteInfoRep noteInfoRep, UNoteRepM noteRep) {
        this.noteInfoRep = noteInfoRep;
        this.noteRep = noteRep;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (mongoUri == null || mongoUri.isBlank()) {
            log.warn("note content migration skipped: mongo uri is empty");
            return;
        }

        long total = 0;
        long success = 0;
        long skipped = 0;
        long failed = 0;

        ConnectionString connectionString = new ConnectionString(mongoUri);
        String targetDatabase = database;
        if ((targetDatabase == null || targetDatabase.isBlank()) && connectionString.getDatabase() != null) {
            targetDatabase = connectionString.getDatabase();
        }

        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase mongoDatabase = mongoClient.getDatabase(targetDatabase);
            MongoCollection<Document> notes = mongoDatabase.getCollection(collection);

            for (Document document : notes.find()) {
                total++;
                try {
                    Long noteId = readNoteId(document.get("noteId"));
                    if (noteId == null || !noteInfoRep.existsById(noteId)) {
                        skipped++;
                        continue;
                    }

                    Note note = noteRep.findById(noteId).orElseGet(() -> {
                        Note created = new Note();
                        created.setNoteId(noteId);
                        return created;
                    });
                    note.setContent(document.getString("content") == null ? "" : document.getString("content"));
                    note.setLastSavedAt(readLastSavedAt(document.get("lastSavedAt")));
                    noteRep.save(note);
                    success++;
                } catch (Exception ex) {
                    failed++;
                    log.error("failed to migrate note document: {}", document.toJson(), ex);
                }
            }
        } catch (Exception ex) {
            log.error("note content migration aborted", ex);
            return;
        }

        log.info("note content migration finished: total={}, success={}, skipped={}, failed={}", total, success, skipped, failed);
    }

    private Long readNoteId(Object raw) {
        if (raw instanceof Number number) {
            return number.longValue();
        }
        if (raw instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    private LocalDateTime readLastSavedAt(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (raw instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        if (raw instanceof String text && !text.isBlank()) {
            return LocalDateTime.parse(text);
        }
        return null;
    }
}
