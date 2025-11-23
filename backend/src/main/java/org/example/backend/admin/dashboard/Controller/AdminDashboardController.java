package org.example.backend.admin.dashboard.Controller;

import jakarta.validation.Valid;
import org.example.backend.admin.dashboard.dto.DashboardMetricsDto;
import org.example.backend.admin.dashboard.request.AddTodoRequest;
import org.example.backend.admin.dashboard.request.SearchRecentRequest;
import org.example.backend.admin.dashboard.request.UpdateTodoRequest;
import org.example.backend.admin.dashboard.service.AdminDashboardService;
import org.example.backend.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) {
        this.service = service;
    }

    @GetMapping("/metrics")
    public ResponseEntity<Object> metrics() {
        DashboardMetricsDto dto = service.metrics();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取仪表盘指标")
                .data(dto)
                .build());
    }

    @PostMapping("/recent")
    public ResponseEntity<Object> recent(@RequestBody SearchRecentRequest request) {
        List<?> list = service.recent(request);
        long total = service.countRecent(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取近期数据")
                .data(data)
                .build());
    }

    @PostMapping("/todo/add")
    public ResponseEntity<Object> addTodo(@Valid @RequestBody AddTodoRequest request) {
        var t = service.addTodo(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("添加成功")
                .data(t)
                .build());
    }

    @PutMapping("/todo/update")
    public ResponseEntity<Object> updateTodo(@RequestBody UpdateTodoRequest request) {
        boolean ok = service.updateTodo(request);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 404)
                .message(ok ? "修改成功" : "记录不存在")
                .build());
    }

    @DeleteMapping("/todo/delete")
    public ResponseEntity<Object> deleteTodo(@RequestParam Long id) {
        boolean ok = service.deleteTodo(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 404)
                .message(ok ? "删除成功" : "记录不存在")
                .build());
    }

    @PostMapping("/todo/batchDelete")
    public ResponseEntity<Object> batchDeleteTodo(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse.Builder<>().status(404).message("列表为空").build());
        }
        boolean ok = service.batchDeleteTodo(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/todo/batchEnable")
    public ResponseEntity<Object> batchEnableTodo(@RequestBody List<Long> ids) {
        boolean ok = service.batchUpdateTodoStatus(ids, 1);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量启用成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/todo/batchDisable")
    public ResponseEntity<Object> batchDisableTodo(@RequestBody List<Long> ids) {
        boolean ok = service.batchUpdateTodoStatus(ids, 0);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量禁用成功" : "无法连接服务器")
                .build());
    }
}