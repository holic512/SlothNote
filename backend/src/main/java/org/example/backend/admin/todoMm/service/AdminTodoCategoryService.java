package org.example.backend.admin.todoMm.service;

import jakarta.persistence.criteria.Predicate;
import org.example.backend.admin.repository.AdminTodoCategoryRepository;
import org.example.backend.admin.todoMm.dto.TodoCategoryDto;
import org.example.backend.admin.todoMm.request.AddTodoCategoryRequest;
import org.example.backend.admin.todoMm.request.SearchTodoCategoryRequest;
import org.example.backend.admin.todoMm.request.UpdateTodoCategoryRequest;
import org.example.backend.common.entity.TodoCategory;
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
public class AdminTodoCategoryService {

    private final AdminTodoCategoryRepository repo;

    @Autowired
    public AdminTodoCategoryService(AdminTodoCategoryRepository repo) { this.repo = repo; }

    public long getCount() { return repo.count(); }

    public List<TodoCategoryDto> fetchInitial(int count) {
        PageRequest pr = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<TodoCategoryDto> findInRange(int pageNum, int pageSize) {
        PageRequest pr = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pr).getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public TodoCategoryDto detail(Long id) { return repo.findById(id).map(this::toDto).orElse(null); }

    public boolean add(AddTodoCategoryRequest req) {
        try {
            TodoCategory e = new TodoCategory();
            e.setUser_id(req.getUserId());
            e.setType(req.getType());
            e.setName(req.getName());
            repo.save(e);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean update(UpdateTodoCategoryRequest req) {
        try {
            Optional<TodoCategory> opt = repo.findById(req.getId());
            if (opt.isEmpty()) return false;
            TodoCategory e = opt.get();
            if (req.getUserId() != null) e.setUser_id(req.getUserId());
            if (req.getType() != null) e.setType(req.getType());
            if (req.getName() != null) e.setName(req.getName());
            if (req.getIsDeleted() != null) e.setIsDeleted(req.getIsDeleted());
            repo.save(e);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean delete(Long id) {
        Optional<TodoCategory> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        TodoCategory e = opt.get();
        e.setIsDeleted(true);
        repo.save(e);
        return true;
    }

    public boolean batchDelete(List<Long> ids) {
        try {
            List<TodoCategory> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(true));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public boolean batchRestore(List<Long> ids) {
        try {
            List<TodoCategory> list = repo.findAllById(ids);
            if (list.isEmpty()) return false;
            list.forEach(i -> i.setIsDeleted(false));
            repo.saveAll(list);
            return true;
        } catch (Exception ex) { return false; }
    }

    public List<TodoCategoryDto> search(SearchTodoCategoryRequest req) {
        Page<TodoCategory> page = repo.findAll(buildSpec(req), PageRequest.of(req.getPageNum() - 1, req.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    public long countSearch(SearchTodoCategoryRequest req) { return repo.count(buildSpec(req)); }

    private Specification<TodoCategory> buildSpec(SearchTodoCategoryRequest req) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getQ() != null && !req.getQ().isEmpty()) {
                String pattern = "%" + req.getQ().trim() + "%";
                ps.add(cb.like(root.get("name"), pattern));
            }
            if (req.getUserId() != null) ps.add(cb.equal(root.get("user_id"), req.getUserId()));
            if (req.getType() != null) ps.add(cb.equal(root.get("type"), req.getType()));
            if (req.getIsDeleted() != null) ps.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private TodoCategoryDto toDto(TodoCategory e) {
        TodoCategoryDto d = new TodoCategoryDto();
        d.setId(e.getId());
        d.setUserId(e.getUser_id());
        d.setType(e.getType());
        d.setName(e.getName());
        d.setIsDeleted(e.getIsDeleted());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}