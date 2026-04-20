package org.example.backend.admin.noteMm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.example.backend.admin.noteMm.enums.AdminNoteMmEnum;
import org.example.backend.admin.noteMm.dto.NoteInfoDto;
import org.example.backend.admin.noteMm.request.AddNoteRequest;
import org.example.backend.admin.noteMm.request.SearchNoteRequest;
import org.example.backend.admin.noteMm.request.UpdateNoteRequest;
import org.example.backend.admin.repository.AdminFolderInfoRepository;
import org.example.backend.admin.repository.AdminNoteInfoRepository;
import org.example.backend.admin.repository.AdminNoteRepository;
import org.example.backend.admin.repository.AdminUserRepository;
import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.FolderInfo;
import org.example.backend.common.entity.NoteInfo;
import org.example.backend.common.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminNoteMmService {

    private final AdminNoteInfoRepository repo;
    private final AdminNoteRepository noteRepo;
    private final AdminUserRepository userRepo;
    private final AdminFolderInfoRepository folderRepo;
    private final ObjectMapper objectMapper;

    @Autowired
    public AdminNoteMmService(
            AdminNoteInfoRepository repo,
            AdminNoteRepository noteRepo,
            AdminUserRepository userRepo,
            AdminFolderInfoRepository folderRepo,
            ObjectMapper objectMapper
    ) {
        this.repo = repo;
        this.noteRepo = noteRepo;
        this.userRepo = userRepo;
        this.folderRepo = folderRepo;
        this.objectMapper = objectMapper;
    }

    public long getCount() { return repo.count(); }

    public List<NoteInfoDto> fetchInitial(int count) {
        PageRequest pr = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toDtos(repo.findAll(pr).getContent());
    }

    public List<NoteInfoDto> findInRange(int pageNum, int pageSize) {
        PageRequest pr = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toDtos(repo.findAll(pr).getContent());
    }

    public NoteInfoDto detail(Long id) { return repo.findById(id).map(this::toDtoWithMeta).orElse(null); }

    public AdminNoteMmEnum add(AddNoteRequest req) {
        try {
            var validation = validateRelation(req.getUserId(), req.getFolderId());
            if (validation != AdminNoteMmEnum.Success) {
                return validation;
            }
            NoteInfo e = new NoteInfo();
            e.setUserId(req.getUserId());
            if (req.getFolderId() != null) e.setFolderId(req.getFolderId());
            e.setNoteTitle(req.getNoteTitle());
            e.setNoteSummary(req.getNoteSummary());
            if (req.getNoteAvatar() != null) e.setNoteAvatar(req.getNoteAvatar().toCharArray());
            e.setNote_cover_url(req.getNoteCoverUrl());
            e.setNotePassword(req.getNotePassword());
            e.setNoteType(req.getNoteType());
            repo.save(e);
            return AdminNoteMmEnum.Success;
        } catch (Exception ex) {
            return AdminNoteMmEnum.ServerError;
        }
    }

    public AdminNoteMmEnum update(UpdateNoteRequest req) {
        try {
            Optional<NoteInfo> opt = repo.findById(req.getId());
            if (opt.isEmpty()) return AdminNoteMmEnum.NotFound;
            NoteInfo e = opt.get();
            Long nextUserId = req.getUserId() != null ? req.getUserId() : e.getUserId();
            Long nextFolderId = req.getFolderId() != null ? req.getFolderId() : e.getFolderId();
            var validation = validateRelation(nextUserId, nextFolderId);
            if (validation != AdminNoteMmEnum.Success) {
                return validation;
            }
            if (req.getUserId() != null) e.setUserId(req.getUserId());
            if (req.getFolderId() != null) e.setFolderId(req.getFolderId());
            if (req.getNoteTitle() != null) e.setNoteTitle(req.getNoteTitle());
            if (req.getNoteSummary() != null) e.setNoteSummary(req.getNoteSummary());
            if (req.getNoteAvatar() != null) e.setNoteAvatar(req.getNoteAvatar().toCharArray());
            if (req.getNoteCoverUrl() != null) e.setNote_cover_url(req.getNoteCoverUrl());
            if (req.getNotePassword() != null) e.setNotePassword(req.getNotePassword());
            if (req.getNoteType() != null) e.setNoteType(req.getNoteType());
            if (req.getIsDeleted() != null) e.setIsDeleted(req.getIsDeleted());
            repo.save(e);
            return AdminNoteMmEnum.Success;
        } catch (Exception ex) {
            return AdminNoteMmEnum.ServerError;
        }
    }

    public boolean delete(Long id) {
        Optional<NoteInfo> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        NoteInfo e = opt.get();
        e.setIsDeleted(1);
        repo.save(e);
        return true;
    }

    public boolean batchDelete(List<Long> ids) {
        try {
            List<NoteInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(1));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchRestore(List<Long> ids) {
        try {
            List<NoteInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(0));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean restore(Long id) {
        Optional<NoteInfo> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        NoteInfo e = opt.get();
        e.setIsDeleted(0);
        repo.save(e);
        return true;
    }

    public List<NoteInfoDto> search(SearchNoteRequest req) {
        Page<NoteInfo> page = repo.findAll(buildSpec(req), PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return toDtos(page.getContent());
    }

    public long countSearch(SearchNoteRequest req) { return repo.count(buildSpec(req)); }

    private Specification<NoteInfo> buildSpec(SearchNoteRequest req) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getQ() != null && !req.getQ().isEmpty()) {
                String pattern = "%" + req.getQ().trim() + "%";
                ps.add(cb.or(cb.like(root.get("noteTitle"), pattern), cb.like(root.get("noteSummary"), pattern)));
            }
            if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
            if (req.getFolderId() != null) ps.add(cb.equal(root.get("folderId"), req.getFolderId()));
            if (req.getNoteType() != null) ps.add(cb.equal(root.get("noteType"), req.getNoteType()));
            if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private NoteInfoDto toDto(NoteInfo e) {
        NoteInfoDto d = new NoteInfoDto();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setFolderId(e.getFolderId());
        d.setNoteTitle(e.getNoteTitle());
        d.setNoteSummary(e.getNoteSummary());
        d.setNoteAvatar(e.getNoteAvatar() == null ? null : new String(e.getNoteAvatar()));
        d.setNoteCoverUrl(e.getNote_cover_url());
        d.setNotePassword(e.getNotePassword());
        d.setNoteType(e.getNoteType());
        d.setIsDeleted(e.getIsDeleted());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }

    private NoteInfoDto toDtoWithMeta(NoteInfo e) {
        NoteInfoDto d = toDto(e);
        Optional<Note> note = noteRepo.findById(e.getId());
        d.setHasContent(note.map(this::hasContent).orElse(false));
        d.setLastSavedAt(note.map(Note::getLastSavedAt).orElse(null));
        return d;
    }

    private List<NoteInfoDto> toDtos(List<NoteInfo> entities) {
        List<Long> ids = entities.stream().map(NoteInfo::getId).toList();
        Map<Long, Note> noteMap = new HashMap<>();
        if (!ids.isEmpty()) {
            noteRepo.findAllById(ids).forEach(note -> noteMap.put(note.getNoteId(), note));
        }
        return entities.stream().map(entity -> {
            NoteInfoDto dto = toDto(entity);
            Note note = noteMap.get(entity.getId());
            dto.setHasContent(note != null && hasContent(note));
            dto.setLastSavedAt(note == null ? null : note.getLastSavedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public String getContent(Long noteId) {
        Optional<Note> n = noteRepo.findById(noteId);
        return n.map(Note::getContent).orElse(null);
    }

    public AdminNoteMmEnum updateContent(Long noteId, String content) {
        try {
            if (!repo.existsById(noteId)) {
                return AdminNoteMmEnum.NotFound;
            }
            objectMapper.readTree(content);
            Note n = noteRepo.findById(noteId).orElseGet(() -> {
                Note nn = new Note();
                nn.setNoteId(noteId);
                return nn;
            });
            n.setContent(content == null ? "" : content);
            n.setLastSavedAt(LocalDateTime.now());
            noteRepo.save(n);
            return AdminNoteMmEnum.Success;
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            return AdminNoteMmEnum.InvalidContent;
        } catch (Exception ex) {
            return AdminNoteMmEnum.ServerError;
        }
    }

    public Map<String, Object> getContentMeta(Long noteId) {
        Optional<Note> note = noteRepo.findById(noteId);
        Map<String, Object> data = new HashMap<>();
        data.put("hasContent", note.map(this::hasContent).orElse(false));
        data.put("lastSavedAt", note.map(Note::getLastSavedAt).orElse(null));
        return data;
    }

    public List<Map<String, Object>> userOptions(String q, int limit) {
        String keyword = q == null ? null : q.trim();
        var specification = (Specification<User>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), pattern),
                        cb.like(root.get("email"), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepo.findAll(specification, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")))
                .getContent()
                .stream()
                .map(user -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", user.getId());
                    item.put("username", user.getUsername());
                    item.put("email", user.getEmail());
                    return item;
                })
                .toList();
    }

    public List<Map<String, Object>> folderOptions(String q, Long userId, int limit) {
        String keyword = q == null ? null : q.trim();
        var specification = (Specification<FolderInfo>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (keyword != null && !keyword.isEmpty()) {
                String pattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("folderName"), pattern),
                        cb.like(cb.function("str", String.class, root.get("id")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return folderRepo.findAll(specification, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id")))
                .getContent()
                .stream()
                .map(folder -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", folder.getId());
                    item.put("folderName", folder.getFolderName());
                    item.put("userId", folder.getUserId());
                    return item;
                })
                .toList();
    }

    private AdminNoteMmEnum validateRelation(Long userId, Long folderId) {
        if (userId == null || userRepo.findById(userId).isEmpty()) {
            return AdminNoteMmEnum.UserNotFound;
        }
        if (folderId == null) {
            return AdminNoteMmEnum.Success;
        }
        Optional<FolderInfo> folder = folderRepo.findById(folderId);
        if (folder.isEmpty()) {
            return AdminNoteMmEnum.FolderNotFound;
        }
        if (!folder.get().getUserId().equals(userId)) {
            return AdminNoteMmEnum.FolderUserMismatch;
        }
        return AdminNoteMmEnum.Success;
    }

    private boolean hasContent(Note note) {
        return note.getContent() != null && !note.getContent().isBlank();
    }
}
