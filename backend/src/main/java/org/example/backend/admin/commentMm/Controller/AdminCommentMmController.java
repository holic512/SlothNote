package org.example.backend.admin.commentMm.Controller;

import jakarta.validation.Valid;
import org.example.backend.admin.commentMm.enums.AdminCommentMmEnum;
import org.example.backend.admin.commentMm.request.AddCommentRequest;
import org.example.backend.admin.commentMm.request.SearchCommentRequest;
import org.example.backend.admin.commentMm.request.UpdateCommentRequest;
import org.example.backend.admin.commentMm.service.AdminCommentMmService;
import org.example.backend.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/commentMm")
public class AdminCommentMmController {

    private final AdminCommentMmService service;

    public AdminCommentMmController(AdminCommentMmService service) {
        this.service = service;
    }

    @GetMapping("getCommentCount")
    public ResponseEntity<Object> getCommentCount() {
        long count = service.getCommentCount();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取评论数目")
                .data(count)
                .build());
    }

    @PostMapping("/search")
    public ResponseEntity<Object> search(@RequestBody SearchCommentRequest request) {
        List<?> list = service.searchComments(request);
        long total = service.countSearchComments(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功搜索评论")
                .data(data)
                .build());
    }

    @GetMapping("/detail")
    public ResponseEntity<Object> detail(@RequestParam Long id) {
        var dto = service.getCommentDetail(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取评论详情")
                .data(dto)
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody UpdateCommentRequest request) {
        AdminCommentMmEnum result = service.updateComment(request);
        switch (result) {
            case Success -> {
                return ResponseEntity.ok(new ApiResponse.Builder<>()
                        .status(200)
                        .message("修改成功")
                        .build());
            }
            case NotFound -> {
                return ResponseEntity.ok(new ApiResponse.Builder<>()
                        .status(404)
                        .message("评论不存在")
                        .build());
            }
            default -> {
                return ResponseEntity.ok(new ApiResponse.Builder<>()
                        .status(500)
                        .message("无法连接服务器")
                        .build());
            }
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestParam Long id) {
        boolean ok = service.deleteComment(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/batchDelete")
    public ResponseEntity<Object> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse.Builder<>().status(404).message("评论列表为空").build());
        }
        boolean ok = service.deleteComments(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("fetchInitialComment")
    public ResponseEntity<Object> fetchInitialComment(@RequestParam int count) {
        var data = service.fetchInitialComment(count);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取第一页的数据")
                .data(data)
                .build());
    }

    @GetMapping("/fetchPageData")
    public ResponseEntity<Object> fetchPageData(@RequestParam int pageNum, @RequestParam int pageSize) {
        var list = service.findCommentInRange(pageNum - 1, pageSize);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取分页数据")
                .data(list)
                .build());
    }

    @PostMapping("/addComment")
    public ResponseEntity<Object> addComment(@Valid @RequestBody AddCommentRequest request) {
        AdminCommentMmEnum result = service.addComment(request);
        switch (result) {
            case Success -> {
                return ResponseEntity.ok(new ApiResponse.Builder<>()
                        .status(200)
                        .message("添加成功")
                        .build());
            }
            default -> {
                return ResponseEntity.ok(new ApiResponse.Builder<>()
                        .status(500)
                        .message("无法连接服务器")
                        .build());
            }
        }
    }
}