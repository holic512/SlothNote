package org.example.backend.admin.noteMm.Controller;

import jakarta.validation.Valid;
import org.example.backend.admin.noteMm.request.AddNoteRequest;
import org.example.backend.admin.noteMm.request.SearchNoteRequest;
import org.example.backend.admin.noteMm.request.UpdateNoteRequest;
import org.example.backend.admin.noteMm.service.AdminNoteMmService;
import org.example.backend.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/noteMm")
public class AdminNoteMmController {

    private final AdminNoteMmService service;

    @Autowired
    public AdminNoteMmController(AdminNoteMmService service) { this.service = service; }

    @GetMapping("/getCount")
    public ResponseEntity<Object> getCount() {
        long count = service.getCount();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取笔记数目")
                .data(count)
                .build());
    }

    @PostMapping("/search")
    public ResponseEntity<Object> search(@RequestBody SearchNoteRequest request) {
        var list = service.search(request);
        long total = service.countSearch(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功搜索笔记")
                .data(data)
                .build());
    }

    @GetMapping("/detail")
    public ResponseEntity<Object> detail(@RequestParam Long id) {
        var dto = service.detail(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取笔记详情")
                .data(dto)
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody UpdateNoteRequest request) {
        boolean ok = service.update(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "修改成功" : "无法连接服务器")
                .build());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestParam Long id) {
        boolean ok = service.delete(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/batchDelete")
    public ResponseEntity<Object> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("列表为空")
                    .build());
        }
        boolean ok = service.batchDelete(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/batchRestore")
    public ResponseEntity<Object> batchRestore(@RequestBody List<Long> ids) {
        boolean ok = service.batchRestore(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量恢复成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/fetchInitial")
    public ResponseEntity<Object> fetchInitial(@RequestParam int count) {
        var data = service.fetchInitial(count);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取第一页的数据")
                .data(data)
                .build());
    }

    @GetMapping("/fetchPageData")
    public ResponseEntity<Object> fetchPageData(@RequestParam int pageNum, @RequestParam int pageSize) {
        var data = service.findInRange(pageNum - 1, pageSize);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取分页数据")
                .data(data)
                .build());
    }

    @PostMapping("/add")
    public ResponseEntity<Object> add(@Valid @RequestBody AddNoteRequest request) {
        boolean ok = service.add(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "添加成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/content/get")
    public ResponseEntity<Object> getContent(@RequestParam Long noteId) {
        var content = service.getContent(noteId);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取内容")
                .data(content)
                .build());
    }

    @PostMapping("/content/update")
    public ResponseEntity<Object> updateContent(@RequestBody Map<String, Object> body) {
        Long noteId = Long.valueOf(body.get("noteId").toString());
        String content = (String) body.get("content");
        boolean ok = service.updateContent(noteId, content);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "更新成功" : "无法连接服务器")
                .build());
    }
}