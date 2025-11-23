package org.example.backend.admin.noteMm.service;

import jakarta.persistence.criteria.Predicate;
import org.example.backend.admin.noteMm.dto.NoteInfoDto;
import org.example.backend.admin.noteMm.request.AddNoteRequest;
import org.example.backend.admin.noteMm.request.SearchNoteRequest;
import org.example.backend.admin.noteMm.request.UpdateNoteRequest;
import org.example.backend.admin.repository.AdminNoteInfoRepository;
import org.example.backend.admin.repository.AdminNoteRepository;
import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.NoteInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminNoteMmService {

    private final AdminNoteInfoRepository repo;
    private final AdminNoteRepository noteRepo;

    @Autowired
    public AdminNoteMmService(AdminNoteInfoRepository repo, AdminNoteRepository noteRepo) {
        this.repo = repo;
        this.noteRepo = noteRepo;
    }

    public long getCount() { return repo.count(); }

    public List<NoteInfoDto> fetchInitial(int count) {
        PageRequest pr = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<NoteInfoDto> findInRange(int pageNum, int pageSize) {
        PageRequest pr = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public NoteInfoDto detail(Long id) { return repo.findById(id).map(this::toDto).orElse(null); }

    public boolean add(AddNoteRequest req) {
        try {
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
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean update(UpdateNoteRequest req) {
        try {
            Optional<NoteInfo> opt = repo.findById(req.getId());
            if (opt.isEmpty()) return false;
            NoteInfo e = opt.get();
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
            return true;
        } catch (Exception ex) { return false; }
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

    public List<NoteInfoDto> search(SearchNoteRequest req) {
        Page<NoteInfo> page = repo.findAll(buildSpec(req), PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
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

    public String getContent(Long noteId) {
        Optional<Note> n = noteRepo.findById(noteId);
        return n.map(Note::getContent).orElse(null);
    }

    public boolean updateContent(Long noteId, String content) {
        Note n = noteRepo.findById(noteId).orElseGet(() -> {
            Note nn = new Note();
            nn.setNoteId(noteId);
            return nn;
        });
        n.setContent(content);
        noteRepo.save(n);
        return true;
    }
}