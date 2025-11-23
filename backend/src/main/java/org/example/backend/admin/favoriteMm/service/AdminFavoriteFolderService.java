package org.example.backend.admin.favoriteMm.service;

import jakarta.persistence.criteria.Predicate;
import org.example.backend.admin.favoriteMm.dto.FavoriteFolderDto;
import org.example.backend.admin.favoriteMm.enums.AdminFavoriteFolderEnum;
import org.example.backend.admin.favoriteMm.request.AddFavoriteFolderRequest;
import org.example.backend.admin.favoriteMm.request.SearchFavoriteFolderRequest;
import org.example.backend.admin.favoriteMm.request.UpdateFavoriteFolderRequest;
import org.example.backend.common.entity.FavoriteFolderInfo;
import org.example.backend.admin.repository.AdminFavoriteFolderInfoRepository;
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
public class AdminFavoriteFolderService {

    private final AdminFavoriteFolderInfoRepository repo;

    @Autowired
    public AdminFavoriteFolderService(AdminFavoriteFolderInfoRepository repo) {
        this.repo = repo;
    }

    public long getCount() { return repo.count(); }

    public List<FavoriteFolderDto> fetchInitial(int count) {
        PageRequest pr = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<FavoriteFolderDto> findInRange(int pageNum, int pageSize) {
        PageRequest pr = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public FavoriteFolderDto detail(Long id) { return repo.findById(id).map(this::toDto).orElse(null); }

    public AdminFavoriteFolderEnum add(AddFavoriteFolderRequest req) {
        try {
            FavoriteFolderInfo e = new FavoriteFolderInfo();
            e.setUserId(req.getUserId());
            e.setFolderName(req.getFolderName());
            e.setParentId(req.getParentId());
            e.setFavoriteFolderDescription(req.getFavoriteFolderDescription());
            repo.save(e);
            return AdminFavoriteFolderEnum.Success;
        } catch (Exception ex) {
            return AdminFavoriteFolderEnum.ServerError;
        }
    }

    public AdminFavoriteFolderEnum update(UpdateFavoriteFolderRequest req) {
        try {
            Optional<FavoriteFolderInfo> opt = repo.findById(req.getId());
            if (opt.isEmpty()) return AdminFavoriteFolderEnum.NotFound;
            FavoriteFolderInfo e = opt.get();
            if (req.getUserId() != null) e.setUserId(req.getUserId());
            if (req.getFolderName() != null) e.setFolderName(req.getFolderName());
            if (req.getParentId() != null) e.setParentId(req.getParentId());
            if (req.getFavoriteFolderDescription() != null) e.setFavoriteFolderDescription(req.getFavoriteFolderDescription());
            if (req.getIsDeleted() != null) e.setDeleted(req.getIsDeleted());
            repo.save(e);
            return AdminFavoriteFolderEnum.Success;
        } catch (Exception ex) {
            return AdminFavoriteFolderEnum.ServerError;
        }
    }

    public boolean delete(Long id) {
        Optional<FavoriteFolderInfo> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        FavoriteFolderInfo e = opt.get();
        e.setDeleted(true);
        repo.save(e);
        return true;
    }

    public boolean batchDelete(List<Long> ids) {
        try {
            List<FavoriteFolderInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setDeleted(true));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchRestore(List<Long> ids) {
        try {
            List<FavoriteFolderInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setDeleted(false));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public List<FavoriteFolderDto> search(SearchFavoriteFolderRequest req) {
        Page<FavoriteFolderInfo> page = repo.findAll(buildSpec(req), PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public long countSearch(SearchFavoriteFolderRequest req) { return repo.count(buildSpec(req)); }

    private Specification<FavoriteFolderInfo> buildSpec(SearchFavoriteFolderRequest req) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getQ() != null && !req.getQ().isEmpty()) {
                String pattern = "%" + req.getQ().trim() + "%";
                ps.add(cb.or(
                        cb.like(root.get("folderName"), pattern),
                        cb.like(root.get("favoriteFolderDescription"), pattern)
                ));
            }
            if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
            if (req.getParentId() != null) ps.add(cb.equal(root.get("parentId"), req.getParentId()));
            if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private FavoriteFolderDto toDto(FavoriteFolderInfo e) {
        FavoriteFolderDto d = new FavoriteFolderDto();
        d.setId(e.getId());
        d.setUserId(e.getUserId());
        d.setFolderName(e.getFolderName());
        d.setParentId(e.getParentId());
        d.setFavoriteFolderDescription(e.getFavoriteFolderDescription());
        d.setIsDeleted(e.isDeleted());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}