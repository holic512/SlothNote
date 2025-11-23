package org.example.backend.admin.commentMm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.backend.admin.commentMm.dto.CommentDetailDto;
import org.example.backend.admin.commentMm.enums.AdminCommentMmEnum;
import org.example.backend.admin.commentMm.request.AddCommentRequest;
import org.example.backend.admin.commentMm.request.SearchCommentRequest;
import org.example.backend.admin.commentMm.request.UpdateCommentRequest;
import org.example.backend.admin.repository.AdminCommentRepository;
import org.example.backend.common.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AdminCommentMmService {
    private final AdminCommentRepository repo;

    public AdminCommentMmService(AdminCommentRepository repo) {
        this.repo = repo;
    }

    public long getCommentCount() {
        return repo.count();
    }

    public List<CommentDetailDto> fetchInitialComment(int count) {
        Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> page = repo.findAll(pageable);
        return mapToDtoList(page.getContent());
    }

    public List<CommentDetailDto> findCommentInRange(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> page = repo.findAll(pageable);
        return mapToDtoList(page.getContent());
    }

    public CommentDetailDto getCommentDetail(Long id) {
        Optional<Comment> opt = repo.findById(id);
        return opt.map(this::mapToDto).orElse(null);
    }

    public AdminCommentMmEnum addComment(AddCommentRequest req) {
        try {
            Comment c = new Comment();
            c.setNoteId(req.getNoteId());
            c.setUserId(req.getUserId());
            c.setContent(req.getContent());
            c.setParentId(req.getParentId());
            c.setIsDeleted(false);
            repo.save(c);
            return AdminCommentMmEnum.Success;
        } catch (Exception e) {
            return AdminCommentMmEnum.ServerError;
        }
    }

    public AdminCommentMmEnum updateComment(UpdateCommentRequest req) {
        try {
            Optional<Comment> opt = repo.findById(req.getId());
            if (opt.isEmpty()) return AdminCommentMmEnum.NotFound;
            Comment c = opt.get();
            if (req.getContent() != null && !req.getContent().isEmpty()) c.setContent(req.getContent());
            if (req.getIsDeleted() != null) c.setIsDeleted(req.getIsDeleted());
            repo.save(c);
            return AdminCommentMmEnum.Success;
        } catch (Exception e) {
            return AdminCommentMmEnum.ServerError;
        }
    }

    public boolean deleteComment(Long id) {
        try {
            Optional<Comment> opt = repo.findById(id);
            if (opt.isEmpty()) return false;
            Comment c = opt.get();
            c.setIsDeleted(true);
            repo.save(c);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteComments(List<Long> ids) {
        try {
            List<Comment> list = repo.findAllById(ids);
            list.forEach(c -> c.setIsDeleted(true));
            repo.saveAll(list);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<CommentDetailDto> searchComments(SearchCommentRequest req) {
        Specification<Comment> spec = buildSpec(req);
        Pageable pageable = PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> page = repo.findAll(spec, pageable);
        return mapToDtoList(page.getContent());
    }

    public long countSearchComments(SearchCommentRequest req) {
        Specification<Comment> spec = buildSpec(req);
        return repo.count(spec);
    }

    private Specification<Comment> buildSpec(SearchCommentRequest req) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
            if (req.getQ() != null && !req.getQ().isEmpty()) {
                ps.add(cb.like(cb.lower(root.get("content")), "%" + req.getQ().toLowerCase() + "%"));
            }
            if (req.getNoteId() != null) {
                ps.add(cb.equal(root.get("noteId"), req.getNoteId()));
            }
            if (req.getUserId() != null) {
                ps.add(cb.equal(root.get("userId"), req.getUserId()));
            }
            if (req.getIsDeleted() != null) {
                ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            }
            if (req.getTopLevelOnly() != null && req.getTopLevelOnly()) {
                ps.add(cb.isNull(root.get("parentId")));
            }
            if (req.getStartTime() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getStartTime()));
            }
            if (req.getEndTime() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getEndTime()));
            }
            return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private List<CommentDetailDto> mapToDtoList(List<Comment> list) {
        List<CommentDetailDto> dtoList = new ArrayList<>();
        for (Comment c : list) dtoList.add(mapToDto(c));
        return dtoList;
    }

    private CommentDetailDto mapToDto(Comment c) {
        CommentDetailDto dto = new CommentDetailDto();
        dto.setId(c.getId());
        dto.setNoteId(c.getNoteId());
        dto.setUserId(c.getUserId());
        dto.setContent(c.getContent());
        dto.setParentId(c.getParentId());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setIsDeleted(c.getIsDeleted());
        return dto;
    }
}