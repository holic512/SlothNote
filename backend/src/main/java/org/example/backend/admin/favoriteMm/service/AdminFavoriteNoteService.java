package org.example.backend.admin.favoriteMm.service;

import jakarta.persistence.criteria.Predicate;
import org.example.backend.admin.favoriteMm.dto.FavoriteNoteDto;
import org.example.backend.admin.favoriteMm.enums.AdminFavoriteNoteEnum;
import org.example.backend.admin.favoriteMm.request.AddFavoriteNoteRequest;
import org.example.backend.admin.favoriteMm.request.SearchFavoriteNoteRequest;
import org.example.backend.admin.favoriteMm.request.UpdateFavoriteNoteRequest;
import org.example.backend.common.entity.FavoriteNoteInfo;
import org.example.backend.admin.repository.AdminFavoriteNoteInfoRepository;
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
public class AdminFavoriteNoteService {

    private final AdminFavoriteNoteInfoRepository repo;

    @Autowired
    public AdminFavoriteNoteService(AdminFavoriteNoteInfoRepository repo) { this.repo = repo; }

    public long getCount() { return repo.count(); }

    public List<FavoriteNoteDto> fetchInitial(int count) {
        PageRequest pr = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<FavoriteNoteDto> findInRange(int pageNum, int pageSize) {
        PageRequest pr = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public FavoriteNoteDto detail(Long id) { return repo.findById(id).map(this::toDto).orElse(null); }

    public AdminFavoriteNoteEnum add(AddFavoriteNoteRequest req) {
        try {
            FavoriteNoteInfo e = new FavoriteNoteInfo();
            e.setUserId(req.getUserId());
            e.setNoteId(req.getNoteId());
            e.setFavoriteFolderId(req.getFavoriteFolderId());
            e.setFavoriteStatus(req.getFavoriteStatus() != null ? req.getFavoriteStatus() : true);
            e.setNoteRemark(req.getNoteRemark());
            repo.save(e);
            return AdminFavoriteNoteEnum.Success;
        } catch (Exception ex) { return AdminFavoriteNoteEnum.ServerError; }
    }

    public AdminFavoriteNoteEnum update(UpdateFavoriteNoteRequest req) {
        try {
            Optional<FavoriteNoteInfo> opt = repo.findById(req.getId());
            if (opt.isEmpty()) return AdminFavoriteNoteEnum.NotFound;
            FavoriteNoteInfo e = opt.get();
            if (req.getUserId() != null) e.setUserId(req.getUserId());
            if (req.getNoteId() != null) e.setNoteId(req.getNoteId());
            if (req.getFavoriteFolderId() != null) e.setFavoriteFolderId(req.getFavoriteFolderId());
            if (req.getFavoriteStatus() != null) e.setFavoriteStatus(req.getFavoriteStatus());
            if (req.getNoteRemark() != null) e.setNoteRemark(req.getNoteRemark());
            if (req.getIsDeleted() != null) e.setIsDeleted(req.getIsDeleted());
            repo.save(e);
            return AdminFavoriteNoteEnum.Success;
        } catch (Exception ex) { return AdminFavoriteNoteEnum.ServerError; }
    }

    public boolean delete(Long id) {
        Optional<FavoriteNoteInfo> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        FavoriteNoteInfo e = opt.get();
        e.setIsDeleted(true);
        repo.save(e);
        return true;
    }

    public boolean batchDelete(List<Long> ids) {
        try {
            List<FavoriteNoteInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(true));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchEnable(List<Long> ids) {
        try {
            List<FavoriteNoteInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setFavoriteStatus(true));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchDisable(List<Long> ids) {
        try {
            List<FavoriteNoteInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setFavoriteStatus(false));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchRestore(List<Long> ids) {
        try {
            List<FavoriteNoteInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(false));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public List<FavoriteNoteDto> search(SearchFavoriteNoteRequest req) {
        Page<FavoriteNoteInfo> page = repo.findAll(buildSpec(req), PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public long countSearch(SearchFavoriteNoteRequest req) { return repo.count(buildSpec(req)); }

    private Specification<FavoriteNoteInfo> buildSpec(SearchFavoriteNoteRequest req) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getQ() != null && !req.getQ().isEmpty()) {
                String pattern = "%" + req.getQ().trim() + "%";
                ps.add(cb.like(root.get("noteRemark"), pattern));
            }
            if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
            if (req.getNoteId() != null) ps.add(cb.equal(root.get("noteId"), req.getNoteId()));
            if (req.getFavoriteFolderId() != null) ps.add(cb.equal(root.get("favoriteFolderId"), req.getFavoriteFolderId()));
            if (req.getFavoriteStatus() != null) ps.add(cb.equal(root.get("favoriteStatus"), req.getFavoriteStatus()));
            if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private FavoriteNoteDto toDto(FavoriteNoteInfo e) {
        FavoriteNoteDto d = new FavoriteNoteDto();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setNoteId(e.getNoteId());
        d.setFavoriteFolderId(e.getFavoriteFolderId());
        d.setFavoriteStatus(e.isFavoriteStatus());
        d.setNoteRemark(e.getNoteRemark());
        d.setIsDeleted(e.getIsDeleted());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}