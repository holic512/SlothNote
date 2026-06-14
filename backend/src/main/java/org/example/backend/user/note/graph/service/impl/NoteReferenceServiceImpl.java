/**
 * @file NoteReferenceServiceImpl
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 实现笔记引用同步与知识星图数据组装。
 * @logic 1. 递归扫描 Tiptap JSON 中的 noteReference 节点；2. 保存时重建当前笔记出边；3. 聚合 note_info 与 note_reference 生成图谱。
 * @dependencies Repository: NoteReferenceRepository/NoteGraphNoteInfoRepository, Jackson: ObjectMapper
 * @index_tags 笔记引用同步, Tiptap JSON, 知识图谱, noteReference
 * @author holic512
 */
package org.example.backend.user.note.graph.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.common.entity.NoteInfo;
import org.example.backend.common.entity.NoteReference;
import org.example.backend.user.note.graph.dto.NoteGraphDto;
import org.example.backend.user.note.graph.dto.NoteGraphEdgeDto;
import org.example.backend.user.note.graph.dto.NoteGraphNodeDto;
import org.example.backend.user.note.graph.repository.NoteGraphNoteInfoRepository;
import org.example.backend.user.note.graph.repository.NoteReferenceRepository;
import org.example.backend.user.note.graph.service.NoteReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NoteReferenceServiceImpl implements NoteReferenceService {

    private final NoteReferenceRepository noteReferenceRepository;
    private final NoteGraphNoteInfoRepository noteInfoRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public NoteReferenceServiceImpl(NoteReferenceRepository noteReferenceRepository,
                                    NoteGraphNoteInfoRepository noteInfoRepository,
                                    ObjectMapper objectMapper) {
        this.noteReferenceRepository = noteReferenceRepository;
        this.noteInfoRepository = noteInfoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void syncReferencesFromContent(Long userId, Long sourceNoteId, String contentJson) {
        if (userId == null || sourceNoteId == null) {
            return;
        }

        noteReferenceRepository.deleteByUserIdAndSourceNoteId(userId, sourceNoteId);

        Set<Long> targetIds = extractReferenceIds(contentJson);
        targetIds.remove(sourceNoteId);
        if (targetIds.isEmpty()) {
            return;
        }

        Map<Long, NoteInfo> targetNoteMap = noteInfoRepository.findByUserIdAndIdInAndIsDeleted(userId, targetIds, 0)
                .stream()
                .collect(Collectors.toMap(NoteInfo::getId, Function.identity()));

        List<NoteReference> references = targetIds.stream()
                .map(targetNoteMap::get)
                .filter(Objects::nonNull)
                .map(targetNote -> {
                    NoteReference reference = new NoteReference();
                    reference.setUserId(userId);
                    reference.setSourceNoteId(sourceNoteId);
                    reference.setTargetNoteId(targetNote.getId());
                    reference.setLabelSnapshot(targetNote.getNoteTitle());
                    return reference;
                })
                .toList();

        noteReferenceRepository.saveAll(references);
    }

    @Override
    public NoteGraphDto getGraph(Long userId, Long currentNoteId) {
        List<NoteInfo> notes = noteInfoRepository.findByUserIdAndIsDeleted(userId, 0);
        List<NoteReference> references = noteReferenceRepository.findByUserId(userId);
        Set<Long> noteIds = notes.stream().map(NoteInfo::getId).collect(Collectors.toSet());

        List<NoteGraphEdgeDto> edges = references.stream()
                .filter(edge -> noteIds.contains(edge.getSourceNoteId()) && noteIds.contains(edge.getTargetNoteId()))
                .map(edge -> new NoteGraphEdgeDto(edge.getSourceNoteId(), edge.getTargetNoteId(), edge.getLabelSnapshot()))
                .toList();

        Map<Long, Integer> degreeMap = new HashMap<>();
        for (NoteGraphEdgeDto edge : edges) {
            degreeMap.merge(edge.getSourceNoteId(), 1, Integer::sum);
            degreeMap.merge(edge.getTargetNoteId(), 1, Integer::sum);
        }

        List<NoteGraphNodeDto> nodes = notes.stream()
                .map(note -> new NoteGraphNodeDto(
                        note.getId(),
                        normalizeTitle(note.getNoteTitle()),
                        note.getNoteSummary(),
                        note.getNoteAvatar() == null ? "" : new String(note.getNoteAvatar()),
                        note.getFolderId(),
                        degreeMap.getOrDefault(note.getId(), 0),
                        Objects.equals(note.getId(), currentNoteId)
                ))
                .toList();

        return new NoteGraphDto(currentNoteId, nodes, edges);
    }

    private Set<Long> extractReferenceIds(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return new LinkedHashSet<>();
        }

        try {
            JsonNode root = objectMapper.readTree(contentJson);
            Set<Long> ids = new LinkedHashSet<>();
            collectReferenceIds(root, ids);
            return ids;
        } catch (Exception ignored) {
            return new LinkedHashSet<>();
        }
    }

    private void collectReferenceIds(JsonNode node, Set<Long> ids) {
        if (node == null || node.isNull()) {
            return;
        }

        if ("noteReference".equals(node.path("type").asText())) {
            JsonNode noteIdNode = node.path("attrs").path("noteId");
            if (noteIdNode.canConvertToLong()) {
                ids.add(noteIdNode.asLong());
            }
        }

        JsonNode content = node.path("content");
        if (content.isArray()) {
            content.forEach(child -> collectReferenceIds(child, ids));
        }
    }

    private String normalizeTitle(String title) {
        return title == null || title.isBlank() ? "新建文档" : title;
    }
}
