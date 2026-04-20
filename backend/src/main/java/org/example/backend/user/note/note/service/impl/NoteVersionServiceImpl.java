package org.example.backend.user.note.note.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.NoteVersion;
import org.example.backend.user.note.note.dto.NoteVersionDetailDto;
import org.example.backend.user.note.note.dto.NoteVersionDto;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.example.backend.user.note.note.repository.UNoteVersionRep;
import org.example.backend.user.note.note.service.NoteVersionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class NoteVersionServiceImpl implements NoteVersionService {

    private final UNoteVersionRep noteVersionRep;
    private final UNoteRepM uNoteRepM;
    private final ObjectMapper objectMapper;

    public NoteVersionServiceImpl(UNoteVersionRep noteVersionRep, UNoteRepM uNoteRepM, ObjectMapper objectMapper) {
        this.noteVersionRep = noteVersionRep;
        this.uNoteRepM = uNoteRepM;
        this.objectMapper = objectMapper;
    }

    @Override
    public void createVersionIfChanged(Long userId, Long noteId, String contentJson, String sourceType) {
        if (contentJson == null || contentJson.isBlank()) {
            return;
        }
        Optional<NoteVersion> latest = noteVersionRep.findTopByNoteIdAndUserIdOrderByVersionNoDesc(noteId, userId);
        if (latest.isPresent() && contentJson.equals(latest.get().getContentJson())) {
            return;
        }

        NoteVersion version = new NoteVersion();
        version.setNoteId(noteId);
        version.setUserId(userId);
        version.setVersionNo(latest.map(item -> item.getVersionNo() + 1).orElse(1));
        version.setContentJson(contentJson);
        version.setContentPreview(extractPreview(contentJson));
        version.setSourceType(sourceType == null ? "SAVE" : sourceType);
        noteVersionRep.save(version);
    }

    @Override
    public List<NoteVersionDto> listVersions(Long userId, Long noteId) {
        return noteVersionRep.findTop20ByNoteIdAndUserIdOrderByVersionNoDesc(noteId, userId).stream()
                .map(version -> new NoteVersionDto(
                        version.getId(),
                        version.getVersionNo(),
                        version.getContentPreview(),
                        version.getSourceType(),
                        version.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public NoteVersionDetailDto getVersionDetail(Long userId, Long noteId, Long versionId) {
        return noteVersionRep.findByIdAndNoteIdAndUserId(versionId, noteId, userId)
                .map(version -> new NoteVersionDetailDto(
                        version.getId(),
                        version.getVersionNo(),
                        version.getContentJson(),
                        version.getContentPreview(),
                        version.getSourceType(),
                        version.getCreatedAt()
                ))
                .orElse(null);
    }

    @Override
    public Note restoreVersion(Long userId, Long noteId, Long versionId) {
        NoteVersion version = noteVersionRep.findByIdAndNoteIdAndUserId(versionId, noteId, userId).orElse(null);
        if (version == null) {
            return null;
        }

        Note note = uNoteRepM.findById(noteId).orElseGet(() -> {
            Note created = new Note();
            created.setNoteId(noteId);
            return created;
        });
        note.setContent(version.getContentJson() == null ? "" : version.getContentJson());
        note.setLastSavedAt(LocalDateTime.now());
        Note saved = uNoteRepM.save(note);

        createVersionIfChanged(userId, noteId, version.getContentJson(), "RESTORE");
        return saved;
    }

    private String extractPreview(String contentJson) {
        try {
            JsonNode root = objectMapper.readTree(contentJson);
            List<String> parts = new ArrayList<>();
            collectText(root, parts);
            String text = String.join(" ", parts).replaceAll("\\s+", " ").trim();
            if (text.isBlank()) {
                return "空白内容";
            }
            return text.length() > 120 ? text.substring(0, 120) + "..." : text;
        } catch (Exception ignored) {
            String normalized = contentJson.replaceAll("\\s+", " ").trim();
            return normalized.length() > 120 ? normalized.substring(0, 120) + "..." : normalized;
        }
    }

    private void collectText(JsonNode node, List<String> parts) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            JsonNode textNode = node.get("text");
            if (textNode != null && textNode.isTextual()) {
                parts.add(textNode.asText());
            }
            Iterator<JsonNode> iterator = node.elements();
            while (iterator.hasNext()) {
                collectText(iterator.next(), parts);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectText(child, parts);
            }
        }
    }
}
