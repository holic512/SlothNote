package org.example.backend.admin.todoMm.Controller;

import jakarta.validation.Valid;
import org.example.backend.admin.todoMm.request.*;
import org.example.backend.admin.todoMm.service.AdminTodoCategoryService;
import org.example.backend.admin.todoMm.service.AdminTodoService;
import org.example.backend.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/todoMm")
public class AdminTodoMmController {

    private final AdminTodoCategoryService categoryService;
    private final AdminTodoService todoService;

    @Autowired
    public AdminTodoMmController(AdminTodoCategoryService categoryService, AdminTodoService todoService) {
        this.categoryService = categoryService;
        this.todoService = todoService;
    }

    @GetMapping("/category/getCount")
    public ResponseEntity<Object> categoryGetCount() {
        long count = categoryService.getCount();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取待做分类数目")
                .data(count)
                .build());
    }

    @PostMapping("/category/search")
    public ResponseEntity<Object> categorySearch(@RequestBody SearchTodoCategoryRequest request) {
        var list = categoryService.search(request);
        long total = categoryService.countSearch(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功搜索待做分类")
                .data(data)
                .build());
    }

    @GetMapping("/category/detail")
    public ResponseEntity<Object> categoryDetail(@RequestParam Long id) {
        var dto = categoryService.detail(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取待做分类详情")
                .data(dto)
                .build());
    }

    @PutMapping("/category/update")
    public ResponseEntity<Object> categoryUpdate(@RequestBody UpdateTodoCategoryRequest request) {
        boolean ok = categoryService.update(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "修改成功" : "无法连接服务器")
                .build());
    }

    @DeleteMapping("/category/delete")
    public ResponseEntity<Object> categoryDelete(@RequestParam Long id) {
        boolean ok = categoryService.delete(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/category/batchDelete")
    public ResponseEntity<Object> categoryBatchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("列表为空")
                    .build());
        }
        boolean ok = categoryService.batchDelete(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/category/batchRestore")
    public ResponseEntity<Object> categoryBatchRestore(@RequestBody List<Long> ids) {
        boolean ok = categoryService.batchRestore(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量恢复成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/category/fetchInitial")
    public ResponseEntity<Object> categoryFetchInitial(@RequestParam int count) {
        var data = categoryService.fetchInitial(count);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取第一页的数据")
                .data(data)
                .build());
    }

    @GetMapping("/category/fetchPageData")
    public ResponseEntity<Object> categoryFetchPageData(@RequestParam int pageNum, @RequestParam int pageSize) {
        var data = categoryService.findInRange(pageNum - 1, pageSize);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取分页数据")
                .data(data)
                .build());
    }

    @PostMapping("/category/add")
    public ResponseEntity<Object> categoryAdd(@Valid @RequestBody AddTodoCategoryRequest request) {
        boolean ok = categoryService.add(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "添加成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/todo/getCount")
    public ResponseEntity<Object> todoGetCount() {
        long count = todoService.getCount();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取待做数目")
                .data(count)
                .build());
    }

    @PostMapping("/todo/search")
    public ResponseEntity<Object> todoSearch(@RequestBody SearchTodoRequest request) {
        var list = todoService.search(request);
        long total = todoService.countSearch(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功搜索待做记录")
                .data(data)
                .build());
    }

    @GetMapping("/todo/detail")
    public ResponseEntity<Object> todoDetail(@RequestParam Long id) {
        var dto = todoService.detail(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取待做详情")
                .data(dto)
                .build());
    }

    @PutMapping("/todo/update")
    public ResponseEntity<Object> todoUpdate(@RequestBody UpdateTodoRequest request) {
        boolean ok = todoService.update(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "修改成功" : "无法连接服务器")
                .build());
    }

    @DeleteMapping("/todo/delete")
    public ResponseEntity<Object> todoDelete(@RequestParam Long id) {
        boolean ok = todoService.delete(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/todo/batchDelete")
    public ResponseEntity<Object> todoBatchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("列表为空")
                    .build());
        }
        boolean ok = todoService.batchDelete(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/todo/batchRestore")
    public ResponseEntity<Object> todoBatchRestore(@RequestBody List<Long> ids) {
        boolean ok = todoService.batchRestore(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量恢复成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/todo/batchEnable")
    public ResponseEntity<Object> todoBatchEnable(@RequestBody List<Long> ids) {
        boolean ok = todoService.batchEnable(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量启用成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/todo/batchDisable")
    public ResponseEntity<Object> todoBatchDisable(@RequestBody List<Long> ids) {
        boolean ok = todoService.batchDisable(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量禁用成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/todo/fetchInitial")
    public ResponseEntity<Object> todoFetchInitial(@RequestParam int count) {
        var data = todoService.fetchInitial(count);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取第一页的数据")
                .data(data)
                .build());
    }

    @GetMapping("/todo/fetchPageData")
    public ResponseEntity<Object> todoFetchPageData(@RequestParam int pageNum, @RequestParam int pageSize) {
        var data = todoService.findInRange(pageNum - 1, pageSize);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取分页数据")
                .data(data)
                .build());
    }

    @PostMapping("/todo/add")
    public ResponseEntity<Object> todoAdd(@Valid @RequestBody AddTodoRequest request) {
        boolean ok = todoService.add(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "添加成功" : "无法连接服务器")
                .build());
    }
}