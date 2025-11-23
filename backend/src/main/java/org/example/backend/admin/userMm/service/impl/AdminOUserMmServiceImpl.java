/**
 * File Name: AdminOUserMmServiceImpl.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-09-23
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.admin.userMm.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.example.backend.admin.repository.AdminUserRepository;
import org.example.backend.admin.userMm.dto.OUserInfoDto;
import org.example.backend.admin.userMm.request.FetchPageData;
import org.example.backend.admin.userMm.service.AdminOUserMmService;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AdminOUserMmServiceImpl implements AdminOUserMmService {

    private static RedisTemplate<String, Object> redisTemplate;
    private AdminUserRepository adminUserRepository;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate, AdminUserRepository adminUserRepository) {
        AdminOUserMmServiceImpl.redisTemplate = redisTemplate;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public int fetchOUserCount() {

        int count = 0; // 计数器
        String cursor; // 初始游标

        do {
            // 执行 SCAN 命令
            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match("satoken:user:session:*")  // 匹配模式
                    .count(100)
                    .build();
            Cursor<String> scanResult = redisTemplate.scan(scanOptions);

            while (scanResult.hasNext()) {
                scanResult.next(); // 获取当前键
                count++; // 增加计数器
            }

            // 更新游标
            cursor = scanResult.getId().getCursorId(); // 更新游标

        } while (!cursor.equals("0")); // 如果游标为0，表示遍历结束

        return count; // 返回匹配的键的数量
    }

    @Override
    public ResponseEntity<Object> fetchPageDate(FetchPageData fetchPageData) {
        int page = fetchPageData.getPage();
        int rows = fetchPageData.getRows();

        List<String> sessionIdList = StpKit.USER.searchSessionId("", (page - 1) * rows, rows, false);
        List<String> OUserIds = new ArrayList<>();
        Map<String, String> createTimeMap = new HashMap<>();

        // 根据获取的session会话 获取信息
        // 获取 用户名 邮箱地址 并且查询 创建地址
        for (String sessionId : sessionIdList) {
            SaSession session = StpUtil.getSessionBySessionId(sessionId);
            // 获取uid 创建时间
            String uid = (String) session.getLoginId();
            long createTime = session.getCreateTime();

            // 格式化时间
            Date date = new Date(createTime);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = formatter.format(date);

            OUserIds.add(uid);
            createTimeMap.put(uid, formattedDate);
        }
        // id uid 用户名 邮箱地址
        List<OUserInfoDto> oUserInfoDtos = adminUserRepository.findUsernamesAndEmailsByUidList(OUserIds);

        // 将创建时间插入
        for (OUserInfoDto oUserInfoDto : oUserInfoDtos) {
            oUserInfoDto.setCreateTime(createTimeMap.get(oUserInfoDto.getUid()));
        }

        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("SUCCESS FETCH PAGE DATA")
                .data(oUserInfoDtos)
                .build()
        );
    }

    @Override
    public ResponseEntity<Object> logout(String uid) {
        StpKit.USER.logout(uid);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Object> kickout(String uid) {
        StpKit.USER.kickout(uid);
        return ResponseEntity.ok().build();
    }
}
