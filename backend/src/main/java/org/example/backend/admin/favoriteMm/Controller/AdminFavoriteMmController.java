package org.example.backend.admin.favoriteMm.Controller;

import jakarta.validation.Valid;
import org.example.backend.admin.favoriteMm.enums.AdminFavoriteFolderEnum;
import org.example.backend.admin.favoriteMm.enums.AdminFavoriteNoteEnum;
import org.example.backend.admin.favoriteMm.request.*;
import org.example.backend.admin.favoriteMm.service.AdminFavoriteFolderService;
import org.example.backend.admin.favoriteMm.service.AdminFavoriteNoteService;
import org.example.backend.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/favoriteMm")
public class AdminFavoriteMmController {

    private final AdminFavoriteFolderService folderService;
    private final AdminFavoriteNoteService noteService;

    @Autowired
    public AdminFavoriteMmController(AdminFavoriteFolderService folderService, AdminFavoriteNoteService noteService) {
        this.folderService = folderService;
        this.noteService = noteService;
    }

    @GetMapping("/folder/getCount")
    public ResponseEntity<Object> folderGetCount() {
        long count = folderService.getCount();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取收藏文件夹数目")
                .data(count)
                .build());
    }

    @PostMapping("/folder/search")
    public ResponseEntity<Object> folderSearch(@RequestBody SearchFavoriteFolderRequest request) {
        var list = folderService.search(request);
        long total = folderService.countSearch(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功搜索收藏文件夹")
                .data(data)
                .build());
    }

    @GetMapping("/folder/detail")
    public ResponseEntity<Object> folderDetail(@RequestParam Long id) {
        var dto = folderService.detail(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取收藏文件夹详情")
                .data(dto)
                .build());
    }

    @PutMapping("/folder/update")
    public ResponseEntity<Object> folderUpdate(@RequestBody UpdateFavoriteFolderRequest request) {
        AdminFavoriteFolderEnum r = folderService.update(request);
        return switch (r) {
            case Success -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("修改成功")
                    .build());
            case NotFound -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("不存在")
                    .build());
            default -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("无法连接服务器")
                    .build());
        };
    }

    @DeleteMapping("/folder/delete")
    public ResponseEntity<Object> folderDelete(@RequestParam Long id) {
        boolean ok = folderService.delete(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/folder/batchDelete")
    public ResponseEntity<Object> folderBatchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("列表为空")
                    .build());
        }
        boolean ok = folderService.batchDelete(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/folder/batchRestore")
    public ResponseEntity<Object> folderBatchRestore(@RequestBody List<Long> ids) {
        boolean ok = folderService.batchRestore(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量恢复成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/folder/fetchInitial")
    public ResponseEntity<Object> folderFetchInitial(@RequestParam int count) {
        var data = folderService.fetchInitial(count);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取第一页的数据")
                .data(data)
                .build());
    }

    @GetMapping("/folder/fetchPageData")
    public ResponseEntity<Object> folderFetchPageData(@RequestParam int pageNum, @RequestParam int pageSize) {
        var data = folderService.findInRange(pageNum - 1, pageSize);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取分页数据")
                .data(data)
                .build());
    }

    @PostMapping("/folder/add")
    public ResponseEntity<Object> folderAdd(@Valid @RequestBody AddFavoriteFolderRequest request) {
        AdminFavoriteFolderEnum r = folderService.add(request);
        return switch (r) {
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

    @GetMapping("/note/getCount")
    public ResponseEntity<Object> noteGetCount() {
        long count = noteService.getCount();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取收藏记录数目")
                .data(count)
                .build());
    }

    @PostMapping("/note/search")
    public ResponseEntity<Object> noteSearch(@RequestBody SearchFavoriteNoteRequest request) {
        var list = noteService.search(request);
        long total = noteService.countSearch(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功搜索收藏记录")
                .data(data)
                .build());
    }

    @GetMapping("/note/detail")
    public ResponseEntity<Object> noteDetail(@RequestParam Long id) {
        var dto = noteService.detail(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取收藏记录详情")
                .data(dto)
                .build());
    }

    @PutMapping("/note/update")
    public ResponseEntity<Object> noteUpdate(@RequestBody UpdateFavoriteNoteRequest request) {
        AdminFavoriteNoteEnum r = noteService.update(request);
        return switch (r) {
            case Success -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("修改成功")
                    .build());
            case NotFound -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("不存在")
                    .build());
            default -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("无法连接服务器")
                    .build());
        };
    }

    @DeleteMapping("/note/delete")
    public ResponseEntity<Object> noteDelete(@RequestParam Long id) {
        boolean ok = noteService.delete(id);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/note/batchDelete")
    public ResponseEntity<Object> noteBatchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("列表为空")
                    .build());
        }
        boolean ok = noteService.batchDelete(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量删除成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/note/batchEnable")
    public ResponseEntity<Object> noteBatchEnable(@RequestBody List<Long> ids) {
        boolean ok = noteService.batchEnable(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量启用成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/note/batchDisable")
    public ResponseEntity<Object> noteBatchDisable(@RequestBody List<Long> ids) {
        boolean ok = noteService.batchDisable(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量禁用成功" : "无法连接服务器")
                .build());
    }

    @PostMapping("/note/batchRestore")
    public ResponseEntity<Object> noteBatchRestore(@RequestBody List<Long> ids) {
        boolean ok = noteService.batchRestore(ids);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(ok ? 200 : 500)
                .message(ok ? "批量恢复成功" : "无法连接服务器")
                .build());
    }

    @GetMapping("/note/fetchInitial")
    public ResponseEntity<Object> noteFetchInitial(@RequestParam int count) {
        var data = noteService.fetchInitial(count);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取第一页的数据")
                .data(data)
                .build());
    }

    @GetMapping("/note/fetchPageData")
    public ResponseEntity<Object> noteFetchPageData(@RequestParam int pageNum, @RequestParam int pageSize) {
        var data = noteService.findInRange(pageNum - 1, pageSize);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取分页数据")
                .data(data)
                .build());
    }

    @PostMapping("/note/add")
    public ResponseEntity<Object> noteAdd(@Valid @RequestBody AddFavoriteNoteRequest request) {
        AdminFavoriteNoteEnum r = noteService.add(request);
        return switch (r) {
            case Success -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("添加成功")
                    .build());
            default -> ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("无法连接服务器")
                    .build());
        };
    }
}