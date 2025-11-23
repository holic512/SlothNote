package org.example.backend.admin.folderMm.Controller;

import jakarta.validation.Valid;
import org.example.backend.admin.folderMm.enums.AdminFolderMmEnum;
import org.example.backend.admin.folderMm.request.AddFolderRequest;
import org.example.backend.admin.folderMm.request.SearchFolderRequest;
import org.example.backend.admin.folderMm.request.UpdateFolderRequest;
import org.example.backend.admin.folderMm.service.AdminFolderMmService;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.admin.userMm.request.SearchUserRequest;
import org.example.backend.admin.userMm.service.AdminUserMmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/folderMm")
public class AdminFolderMmController {

    private final AdminFolderMmService service;
    private final AdminUserMmService adminUserMmService;

    @Autowired
    public AdminFolderMmController(AdminFolderMmService service, AdminUserMmService adminUserMmService) {
        this.service = service;
        this.adminUserMmService = adminUserMmService;
    }

    @GetMapping("/getFolderCount")
    public ResponseEntity<Object> getFolderCount() {
        long count = service.getFolderCount();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取文件夹数目")
                .data(count)
                .build());
    }

    @PostMapping("/search")
    public ResponseEntity<Object> search(@RequestBody SearchFolderRequest request) {
        var list = service.searchFolders(request);
        long total = service.countSearchFolders(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功搜索文件夹")
                .data(data)
                .build());
    }

    @GetMapping("/detail")
    public ResponseEntity<Object> detail(@RequestParam Long id) {
        var dto = service.getFolderDetail(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取文件夹详情")
                .data(dto)
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody UpdateFolderRequest request) {
        AdminFolderMmEnum result = service.updateFolder(request);
        return switch (result) {
            case Success -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("修改成功")
                    .build());
            case NotFound -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("文件夹不存在")
                    .build());
            default -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("无法连接服务器")
                    .build());
        };
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestParam Long id) {
        boolean ok = service.deleteFolder(id);
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
                    .message("文件夹列表为空")
                    .build());
        }
        boolean ok = service.deleteFolders(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/fetchInitialFolder")
    public ResponseEntity<Object> fetchInitialFolder(@RequestParam int count) {
        var data = service.fetchInitialFolder(count);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取第一页的数据")
                .data(data)
                .build());
    }

    @GetMapping("/fetchPageData")
    public ResponseEntity<Object> fetchPageData(@RequestParam int pageNum, @RequestParam int pageSize) {
        var data = service.findFoldersInRange(pageNum - 1, pageSize);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取分页数据")
                .data(data)
                .build());
    }

    @PostMapping("/addFolder")
    public ResponseEntity<Object> addFolder(@Valid @RequestBody AddFolderRequest request) {
        AdminFolderMmEnum result = service.addFolder(request);
        return switch (result) {
            case Success -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("添加成功")
                    .build());
            case FolderAlreadyExists -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(40901)
                    .message("同名文件夹已存在")
                    .build());
            default -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("无法连接服务器")
                    .build());
        };
    }

    @GetMapping("/userOptions")
    public ResponseEntity<Object> userOptions(@RequestParam(required = false) String q, @RequestParam(defaultValue = "50") int limit) {
        SearchUserRequest req = new SearchUserRequest();
        req.setQ(q);
        req.setPageNum(1);
        req.setPageSize(limit);
        var list = adminUserMmService.searchUsers(req);
        var data = list.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            return m;
        }).toList();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取用户选项")
                .data(data)
                .build());
    }
}