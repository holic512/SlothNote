package org.example.backend.admin.todoMm.service;

import jakarta.persistence.criteria.Predicate;
import org.example.backend.admin.repository.AdminTodoInfoRepository;
import org.example.backend.admin.todoMm.dto.TodoInfoDto;
import org.example.backend.admin.todoMm.request.AddTodoRequest;
import org.example.backend.admin.todoMm.request.SearchTodoRequest;
import org.example.backend.admin.todoMm.request.UpdateTodoRequest;
import org.example.backend.common.entity.TodoInfo;
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
public class AdminTodoService {

    private final AdminTodoInfoRepository repo;

    @Autowired
    public AdminTodoService(AdminTodoInfoRepository repo) { this.repo = repo; }

    public long getCount() { return repo.count(); }

    public List<TodoInfoDto> fetchInitial(int count) {
        PageRequest pr = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<TodoInfoDto> findInRange(int pageNum, int pageSize) {
        PageRequest pr = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public TodoInfoDto detail(Long id) { return repo.findById(id).map(this::toDto).orElse(null); }

    public boolean add(AddTodoRequest req) {
        try {
            TodoInfo e = new TodoInfo();
            e.setUser_id(req.getUserId());
            if (req.getCategoryId() != null) e.setCategory_id(req.getCategoryId());
            e.setTitle(req.getTitle());
            e.setDescription(req.getDescription());
            if (req.getStatus() != null) e.setStatus(req.getStatus());
            repo.save(e);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean update(UpdateTodoRequest req) {
        try {
            Optional<TodoInfo> opt = repo.findById(req.getId());
            if (opt.isEmpty()) return false;
            TodoInfo e = opt.get();
            if (req.getUserId() != null) e.setUser_id(req.getUserId());
            if (req.getCategoryId() != null) e.setCategory_id(req.getCategoryId());
            if (req.getTitle() != null) e.setTitle(req.getTitle());
            if (req.getDescription() != null) e.setDescription(req.getDescription());
            if (req.getStatus() != null) e.setStatus(req.getStatus());
            if (req.getIsDeleted() != null) e.setIsDeleted(req.getIsDeleted());
            repo.save(e);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean delete(Long id) {
        Optional<TodoInfo> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        TodoInfo e = opt.get();
        e.setIsDeleted(true);
        repo.save(e);
        return true;
    }

    public boolean batchDelete(List<Long> ids) {
        try {
            List<TodoInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(true));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchRestore(List<Long> ids) {
        try {
            List<TodoInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(false));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchEnable(List<Long> ids) {
        try {
            List<TodoInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setStatus(1));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchDisable(List<Long> ids) {
        try {
            List<TodoInfo> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setStatus(0));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public List<TodoInfoDto> search(SearchTodoRequest req) {
        Page<TodoInfo> page = repo.findAll(buildSpec(req), PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public long countSearch(SearchTodoRequest req) { return repo.count(buildSpec(req)); }

    private Specification<TodoInfo> buildSpec(SearchTodoRequest req) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getQ() != null && !req.getQ().isEmpty()) {
                String pattern = "%" + req.getQ().trim() + "%";
                ps.add(cb.or(cb.like(root.get("title"), pattern), cb.like(root.get("description"), pattern)));
            }
            if (req.getUserId() != null) ps.add(cb.equal(root.get("user_id"), req.getUserId()));
            if (req.getCategoryId() != null) ps.add(cb.equal(root.get("category_id"), req.getCategoryId()));
            if (req.getStatus() != null) ps.add(cb.equal(root.get("status"), req.getStatus()));
            if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private TodoInfoDto toDto(TodoInfo e) {
        TodoInfoDto d = new TodoInfoDto();
        d.setId(e.getId());
        d.setUserId(e.getUser_id());
        d.setCategoryId(e.getCategory_id());
        d.setTitle(e.getTitle());
        d.setDescription(e.getDescription());
        d.setStartDate(e.getStartDate());
        d.setDueDate(e.getDueDate());
        d.setStatus(e.getStatus());
        d.setIsDeleted(e.getIsDeleted());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}