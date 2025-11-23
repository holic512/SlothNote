package org.example.backend.admin.folderMm.service;

import org.example.backend.admin.folderMm.dto.FolderDetailDto;
import org.example.backend.admin.folderMm.enums.AdminFolderMmEnum;
import org.example.backend.admin.folderMm.repository.FolderInfoRepository;
import org.example.backend.admin.folderMm.request.AddFolderRequest;
import org.example.backend.admin.folderMm.request.SearchFolderRequest;
import org.example.backend.admin.folderMm.request.UpdateFolderRequest;
import org.example.backend.common.entity.FolderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminFolderMmService {

    private final FolderInfoRepository folderInfoRepository;

    @Autowired
    public AdminFolderMmService(FolderInfoRepository folderInfoRepository) {
        this.folderInfoRepository = folderInfoRepository;
    }

    public long getFolderCount() {
        return folderInfoRepository.count();
    }

    public List<FolderDetailDto> fetchInitialFolder(int count) {
        PageRequest pr = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FolderInfo> page = folderInfoRepository.findAll(pr);
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<FolderDetailDto> findFoldersInRange(int pageNum, int pageSize) {
        PageRequest pr = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FolderInfo> page = folderInfoRepository.findAll(pr);
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public FolderDetailDto getFolderDetail(Long id) {
        Optional<FolderInfo> opt = folderInfoRepository.findById(id);
        return opt.map(this::toDto).orElse(null);
    }

    public AdminFolderMmEnum addFolder(AddFolderRequest req) {
        try {
            boolean exists = folderInfoRepository.existsByUserIdAndParentIdAndFolderName(req.getUserId(), req.getParentId(), req.getFolderName());
            if (exists) {
                return AdminFolderMmEnum.FolderAlreadyExists;
            }
            FolderInfo info = new FolderInfo();
            info.setUserId(req.getUserId());
            info.setFolderName(req.getFolderName());
            info.setParentId(req.getParentId());
            info.setDescription(req.getDescription());
            if (req.getFolderAvatar() != null) {
                info.setFolderAvatar(req.getFolderAvatar().toCharArray());
            }
            folderInfoRepository.save(info);
            return AdminFolderMmEnum.Success;
        } catch (Exception e) {
            return AdminFolderMmEnum.ServerError;
        }
    }

    public AdminFolderMmEnum updateFolder(UpdateFolderRequest req) {
        try {
            Optional<FolderInfo> opt = folderInfoRepository.findById(req.getId());
            if (opt.isEmpty()) return AdminFolderMmEnum.NotFound;
            FolderInfo info = opt.get();
            if (req.getUserId() != null) info.setUserId(req.getUserId());
            if (req.getFolderName() != null) info.setFolderName(req.getFolderName());
            if (req.getParentId() != null) info.setParentId(req.getParentId());
            if (req.getDescription() != null) info.setDescription(req.getDescription());
            if (req.getFolderAvatar() != null) info.setFolderAvatar(req.getFolderAvatar().toCharArray());
            if (req.getIsDeleted() != null) info.setIsDeleted(req.getIsDeleted());
            folderInfoRepository.save(info);
            return AdminFolderMmEnum.Success;
        } catch (Exception e) {
            return AdminFolderMmEnum.ServerError;
        }
    }

    public boolean deleteFolder(Long id) {
        Optional<FolderInfo> opt = folderInfoRepository.findById(id);
        if (opt.isEmpty()) return false;
        FolderInfo info = opt.get();
        info.setIsDeleted(1);
        folderInfoRepository.save(info);
        return true;
    }

    public boolean deleteFolders(List<Long> ids) {
        try {
            List<FolderInfo> list = folderInfoRepository.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(1));
            folderInfoRepository.saveAll(list);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<FolderDetailDto> searchFolders(SearchFolderRequest req) {
        Page<FolderInfo> page = folderInfoRepository.findAll(buildSpec(req), PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public long countSearchFolders(SearchFolderRequest req) {
        return folderInfoRepository.count(buildSpec(req));
    }

    private Specification<FolderInfo> buildSpec(SearchFolderRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getQ() != null && !req.getQ().isEmpty()) {
                String pattern = "%" + req.getQ().trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("folderName"), pattern),
                        cb.like(root.get("description"), pattern)
                ));
            }
            if (req.getIsDeleted() != null) {
                predicates.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            }
            if (req.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), req.getUserId()));
            }
            if (req.getParentId() != null) {
                predicates.add(cb.equal(root.get("parentId"), req.getParentId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private FolderDetailDto toDto(FolderInfo info) {
        FolderDetailDto dto = new FolderDetailDto();
        dto.setId(info.getId());
        dto.setUserId(info.getUserId());
        dto.setFolderName(info.getFolderName());
        dto.setParentId(info.getParentId());
        dto.setDescription(info.getDescription());
        dto.setFolderAvatar(info.getFolderAvatar() == null ? null : new String(info.getFolderAvatar()));
        dto.setCreatedAt(info.getCreatedAt());
        dto.setUpdatedAt(info.getUpdatedAt());
        dto.setIsDeleted(info.getIsDeleted());
        return dto;
    }
}