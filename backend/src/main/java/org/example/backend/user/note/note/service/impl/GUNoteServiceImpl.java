/**
 * File Name: GUNoteService.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-12
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.note.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.FolderInfo;
import org.example.backend.common.entity.NoteInfo;
import org.example.backend.user.note.note.dto.NoteShareInfoDto;
import org.example.backend.user.note.note.enums.GContextEnum;
import org.example.backend.user.note.note.repository.UNoteInfoRep;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.example.backend.user.note.note.service.GUNoteService;
import org.example.backend.user.repository.UserFolderInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GUNoteServiceImpl implements GUNoteService {

    private final UNoteInfoRep uNoteInfoRep;
    private final UNoteRepM uNoteRepM;
    private final UserFolderInfoRepository userFolderInfoRepository;

    @Autowired
    public GUNoteServiceImpl(UNoteInfoRep uNoteInfoRep,
                             UNoteRepM uNoteRepM,
                             UserFolderInfoRepository userFolderInfoRepository) {
        this.uNoteInfoRep = uNoteInfoRep;
        this.uNoteRepM = uNoteRepM;
        this.userFolderInfoRepository = userFolderInfoRepository;
    }

    @Override
    public Pair<GContextEnum, Optional<Note>> GetContext(Long userId, Long noteId) {
        // 根据笔记Id 查询用户Id
        Long ownerId = uNoteInfoRep.findUserIdByNoteId(noteId);

        if (!ownerId.equals(userId)) {
            return new Pair<>(GContextEnum.NoteOwnerNotMatch, null);
        }

        Optional<Note> note = uNoteRepM.findById(noteId);
        return new Pair<>(GContextEnum.Success, note);
    }

    public String exportHtml(Long userId, Long noteId) {
        Long ownerId = uNoteInfoRep.findUserIdByNoteId(noteId);
        if (ownerId == null || !ownerId.equals(userId)) return null;
        Optional<Note> noteOpt = uNoteRepM.findById(noteId);
        if (noteOpt.isEmpty()) return null;
        String contentJson = noteOpt.get().getContent();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> root = mapper.readValue(contentJson, Map.class);
            String body = renderNode(root);
            String styles = "body{font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#333;line-height:1.6;padding:24px;}" +
                    "h1,h2,h3,h4,h5,h6{margin:12px 0;} p{margin:10px 0;} ul,ol{margin:10px 24px;} pre{background:#f6f8fa;padding:12px;border-radius:4px;overflow:auto;} code{font-family:Consolas,Monaco,monospace;} blockquote{border-left:4px solid #ddd;padding-left:12px;color:#666;margin:10px 0;} img{max-width:100%;}";
            return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Note " + noteId + "</title><style>" + styles + "</style></head><body>" + body + "</body></html>";
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public NoteShareInfoDto getShareInfo(Long userId, Long noteId) {
        Optional<NoteInfo> noteInfoOpt = uNoteInfoRep.findByIdAndUserId(noteId, userId);
        if (noteInfoOpt.isEmpty()) {
            return null;
        }

        NoteInfo noteInfo = noteInfoOpt.get();
        return new NoteShareInfoDto(
                noteInfo.getId(),
                noteInfo.getNoteTitle(),
                buildLocation(noteInfo.getFolderId()),
                noteInfo.getNoteAvatar() == null ? "" : new String(noteInfo.getNoteAvatar()),
                noteInfo.getNote_cover_url()
        );
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private List<String> buildLocation(Long folderId) {
        List<String> locations = new ArrayList<>();
        Long currentFolderId = folderId;

        while (currentFolderId != null && currentFolderId != 0) {
            Optional<FolderInfo> folderOpt = userFolderInfoRepository.findById(currentFolderId);
            if (folderOpt.isEmpty()) {
                break;
            }

            FolderInfo folderInfo = folderOpt.get();
            locations.add(0, folderInfo.getFolderName());
            currentFolderId = folderInfo.getParentId();
        }

        return locations;
    }

    private String renderNode(Object nodeObj) {
        if (!(nodeObj instanceof Map)) return "";
        Map<String, Object> node = (Map<String, Object>) nodeObj;
        String type = (String) node.get("type");
        List<Object> content = (List<Object>) node.get("content");
        Map<String, Object> attrs = (Map<String, Object>) node.getOrDefault("attrs", new LinkedHashMap<>());
        String text = (String) node.get("text");
        List<Map<String, Object>> marks = (List<Map<String, Object>>) node.get("marks");

        if ("doc".equals(type)) {
            if (content == null) return "";
            return content.stream().map(this::renderNode).collect(Collectors.joining());
        }
        if ("paragraph".equals(type)) {
            String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
            return "<p>" + inner + "</p>";
        }
        if ("text".equals(type)) {
            String base = escape(text);
            if (marks != null) {
                for (Map<String, Object> m : marks) {
                    String mk = (String) m.get("type");
                    if ("bold".equals(mk) || "strong".equals(mk)) base = "<strong>" + base + "</strong>";
                    else if ("italic".equals(mk) || "em".equals(mk)) base = "<em>" + base + "</em>";
                    else if ("underline".equals(mk)) base = "<u>" + base + "</u>";
                    else if ("code".equals(mk)) base = "<code>" + base + "</code>";
                    else if ("link".equals(mk)) {
                        Map<String, Object> a = (Map<String, Object>) m.get("attrs");
                        String href = a != null ? (String) a.get("href") : "#";
                        base = "<a href=\"" + escape(href) + "\" target=\"_blank\">" + base + "</a>";
                    }
                }
            }
            return base;
        }
        if ("heading".equals(type)) {
            Object lvl = attrs.get("level");
            int level = (lvl instanceof Number) ? ((Number) lvl).intValue() : 1;
            level = Math.max(1, Math.min(6, level));
            String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
            return "<h" + level + ">" + inner + "</h" + level + ">";
        }
        if ("bullet_list".equals(type)) {
            String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
            return "<ul>" + inner + "</ul>";
        }
        if ("ordered_list".equals(type)) {
            String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
            return "<ol>" + inner + "</ol>";
        }
        if ("list_item".equals(type)) {
            String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
            return "<li>" + inner + "</li>";
        }
        if ("blockquote".equals(type)) {
            String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
            return "<blockquote>" + inner + "</blockquote>";
        }
        if ("code_block".equals(type)) {
            String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
            return "<pre><code>" + inner + "</code></pre>";
        }
        if ("hard_break".equals(type)) {
            return "<br/>";
        }
        if ("image".equals(type)) {
            String src = (String) attrs.getOrDefault("src", "");
            String alt = (String) attrs.getOrDefault("alt", "");
            return "<img src=\"" + escape(src) + "\" alt=\"" + escape(alt) + "\"/>";
        }
        // fallback: render children
        String inner = content == null ? "" : content.stream().map(this::renderNode).collect(Collectors.joining());
        return inner;
    }
}
