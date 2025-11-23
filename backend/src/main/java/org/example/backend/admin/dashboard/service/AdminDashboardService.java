package org.example.backend.admin.dashboard.service;

import java.util.ArrayList;
import java.util.List;

import org.example.backend.admin.dashboard.dto.DashboardMetricsDto;
import org.example.backend.admin.dashboard.request.SearchRecentRequest;
import org.example.backend.admin.dashboard.request.AddTodoRequest;
import org.example.backend.admin.dashboard.request.UpdateTodoRequest;
import org.example.backend.admin.repository.*;
import org.example.backend.common.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    private final AdminUserRepository userRepo;
    private final AdminNoteInfoRepository noteRepo;
    private final AdminFolderInfoRepository folderRepo;
    private final AdminCommentRepository commentRepo;
    private final AdminFavoriteNoteInfoRepository favoriteNoteRepo;
    private final AdminFavoriteFolderInfoRepository favoriteFolderRepo;
    private final AdminTodoInfoRepository todoRepo;

    public AdminDashboardService(AdminUserRepository userRepo,
                                 AdminNoteInfoRepository noteRepo,
                                 AdminFolderInfoRepository folderRepo,
                                 AdminCommentRepository commentRepo,
                                 AdminFavoriteNoteInfoRepository favoriteNoteRepo,
                                 AdminFavoriteFolderInfoRepository favoriteFolderRepo,
                                 AdminTodoInfoRepository todoRepo) {
        this.userRepo = userRepo;
        this.noteRepo = noteRepo;
        this.folderRepo = folderRepo;
        this.commentRepo = commentRepo;
        this.favoriteNoteRepo = favoriteNoteRepo;
        this.favoriteFolderRepo = favoriteFolderRepo;
        this.todoRepo = todoRepo;
    }

    public DashboardMetricsDto metrics() {
        DashboardMetricsDto dto = new DashboardMetricsDto();
        dto.setUserCount(userRepo.count());
        dto.setNoteCount(noteRepo.count());
        dto.setFolderCount(folderRepo.count());
        dto.setCommentCount(commentRepo.count());
        dto.setFavoriteNoteCount(favoriteNoteRepo.count());
        dto.setFavoriteFolderCount(favoriteFolderRepo.count());
        dto.setTodoCount(todoRepo.count());
        return dto;
    }

    public List<?> recent(SearchRecentRequest req) {
        String c = req.getCategory();
        int page = req.getPageNum() - 1;
        Pageable pageable = PageRequest.of(page, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        switch (c) {
            case "comment" -> {
                Specification<Comment> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getQ() != null && !req.getQ().isEmpty()) {
                        ps.add(cb.like(cb.lower(root.get("content")), "%" + req.getQ().toLowerCase() + "%"));
                    }
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                Page<Comment> pageData = commentRepo.findAll(spec, pageable);
                return pageData.getContent();
            }
            case "todo" -> {
                Specification<TodoInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getQ() != null && !req.getQ().isEmpty()) {
                        ps.add(cb.like(cb.lower(root.get("title")), "%" + req.getQ().toLowerCase() + "%"));
                    }
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("user_id"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                Page<TodoInfo> pageData = todoRepo.findAll(spec, pageable);
                return pageData.getContent();
            }
            case "note" -> {
                Specification<NoteInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getQ() != null && !req.getQ().isEmpty()) {
                        ps.add(cb.like(cb.lower(root.get("noteTitle")), "%" + req.getQ().toLowerCase() + "%"));
                    }
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted() ? 1 : 0));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                Page<NoteInfo> pageData = noteRepo.findAll(spec, pageable);
                return pageData.getContent();
            }
            case "favoriteNote" -> {
                Specification<FavoriteNoteInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                Page<FavoriteNoteInfo> pageData = favoriteNoteRepo.findAll(spec, pageable);
                return pageData.getContent();
            }
            case "favoriteFolder" -> {
                Specification<FavoriteFolderInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                Page<FavoriteFolderInfo> pageData = favoriteFolderRepo.findAll(spec, pageable);
                return pageData.getContent();
            }
            case "folder" -> {
                Pageable p = PageRequest.of(page, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<FolderInfo> pageData = folderRepo.findAll(p);
                return pageData.getContent();
            }
            default -> {
                Pageable p = PageRequest.of(page, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<User> pageData = userRepo.findAll(p);
                return pageData.getContent();
            }
        }
    }

    public long countRecent(SearchRecentRequest req) {
        String c = req.getCategory();
        switch (c) {
            case "comment" -> {
                Specification<Comment> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getQ() != null && !req.getQ().isEmpty()) {
                        ps.add(cb.like(cb.lower(root.get("content")), "%" + req.getQ().toLowerCase() + "%"));
                    }
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                return commentRepo.count(spec);
            }
            case "todo" -> {
                Specification<TodoInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getQ() != null && !req.getQ().isEmpty()) {
                        ps.add(cb.like(cb.lower(root.get("title")), "%" + req.getQ().toLowerCase() + "%"));
                    }
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("user_id"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                return todoRepo.count(spec);
            }
            case "note" -> {
                Specification<NoteInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getQ() != null && !req.getQ().isEmpty()) {
                        ps.add(cb.like(cb.lower(root.get("noteTitle")), "%" + req.getQ().toLowerCase() + "%"));
                    }
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted() ? 1 : 0));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                return noteRepo.count(spec);
            }
            case "favoriteNote" -> {
                Specification<FavoriteNoteInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                return favoriteNoteRepo.count(spec);
            }
            case "favoriteFolder" -> {
                Specification<FavoriteFolderInfo> spec = (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> ps = new ArrayList<>();
                    if (req.getUserId() != null) ps.add(cb.equal(root.get("userId"), req.getUserId()));
                    if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
                    return cb.and(ps.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
                return favoriteFolderRepo.count(spec);
            }
            case "folder" -> {
                return folderRepo.count();
            }
            default -> {
                return userRepo.count();
            }
        }
    }

    public TodoInfo addTodo(AddTodoRequest req) {
        TodoInfo t = new TodoInfo();
        t.setUser_id(req.getUserId());
        t.setCategory_id(req.getCategoryId());
        t.setTitle(req.getTitle());
        t.setDescription(req.getDescription());
        if (req.getStatus() != null) t.setStatus(req.getStatus());
        return todoRepo.save(t);
    }

    public boolean updateTodo(UpdateTodoRequest req) {
        return todoRepo.findById(req.getId()).map(t -> {
            if (req.getTitle() != null) t.setTitle(req.getTitle());
            if (req.getDescription() != null) t.setDescription(req.getDescription());
            if (req.getStatus() != null) t.setStatus(req.getStatus());
            if (req.getIsDeleted() != null) t.setIsDeleted(req.getIsDeleted());
            todoRepo.save(t);
            return true;
        }).orElse(false);
    }

    public boolean deleteTodo(Long id) {
        return todoRepo.findById(id).map(t -> {
            t.setIsDeleted(true);
            todoRepo.save(t);
            return true;
        }).orElse(false);
    }

    public boolean batchDeleteTodo(List<Long> ids) {
        try {
            List<TodoInfo> list = todoRepo.findAllById(ids);
            list.forEach(t -> t.setIsDeleted(true));
            todoRepo.saveAll(list);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean batchUpdateTodoStatus(List<Long> ids, int status) {
        try {
            List<TodoInfo> list = todoRepo.findAllById(ids);
            list.forEach(t -> t.setStatus(status));
            todoRepo.saveAll(list);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}