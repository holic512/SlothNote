package org.example.backend.admin.aiMm.Controller;

import org.example.backend.admin.aiMm.request.SearchAiSessionRequest;
import org.example.backend.admin.aiMm.service.AdminAiMmService;
import org.example.backend.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/aiMm")
public class AdminAiMmController {

    private final AdminAiMmService service;

    public AdminAiMmController(AdminAiMmService service) {
        this.service = service;
    }

    @GetMapping("/getCount")
    public ResponseEntity<Object> getCount() {
        return ResponseEntity.ok(new ApiResponse<>(200, "成功获取 AI 会话数目", service.getCount()));
    }

    @PostMapping("/search")
    public ResponseEntity<Object> search(@RequestBody SearchAiSessionRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", service.search(request));
        data.put("total", service.countSearch(request));
        return ResponseEntity.ok(new ApiResponse<>(200, "成功搜索 AI 记录", data));
    }

    @GetMapping("/detail")
    public ResponseEntity<Object> detail(@RequestParam Long id) {
        return ResponseEntity.ok(new ApiResponse<>(200, "成功获取 AI 记录详情", service.detail(id)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestParam Long id) {
        boolean ok = service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(ok ? 200 : 500, ok ? "删除成功" : "无法连接服务器"));
    }

    @PostMapping("/restore")
    public ResponseEntity<Object> restore(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        boolean ok = service.restore(id);
        return ResponseEntity.ok(new ApiResponse<>(ok ? 200 : 500, ok ? "恢复成功" : "无法连接服务器"));
    }

    @PostMapping("/batchDelete")
    public ResponseEntity<Object> batchDelete(@RequestBody List<Long> ids) {
        boolean ok = service.batchDelete(ids);
        return ResponseEntity.ok(new ApiResponse<>(ok ? 200 : 500, ok ? "批量删除成功" : "无法连接服务器"));
    }

    @PostMapping("/batchRestore")
    public ResponseEntity<Object> batchRestore(@RequestBody List<Long> ids) {
        boolean ok = service.batchRestore(ids);
        return ResponseEntity.ok(new ApiResponse<>(ok ? 200 : 500, ok ? "批量恢复成功" : "无法连接服务器"));
    }
}
