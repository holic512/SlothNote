/**
 * File Name: GetCommentServiceImpl.java
 * Description: 评论服务实现类，提供获取指定笔记下的评论及其回复的逻辑
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 * Usage:
 * 实现获取笔记评论和评论回复的服务，封装评论的显示数据，包括评论内容、用户信息、评论回复等。
 */
package org.example.backend.user.note.comment.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.UserAvatar.UserAvatarServiceImpl;
import org.example.backend.user.note.comment.dto.CommentViewPojo;
import org.example.backend.user.note.comment.dto.ReplyViewPojo;
import org.example.backend.user.note.comment.repository.UCommentRep;
import org.example.backend.user.note.comment.service.GetCommentService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCommentServiceImpl implements GetCommentService {

    private final UCommentRep commentRep;
    private final UserAvatarServiceImpl userAvatarService;

    /**
     * 获取指定笔记的所有评论及其回复
     * @param user_id 用户ID，用于权限验证或数据隔离
     * @param note_id 笔记ID，用于查询该笔记的评论
     * @return 评论列表，包括根评论和其下的子评论（回复）
     */
    @Override
    public List<CommentViewPojo> GetComments(Long user_id, Long note_id) {
        // 获取该笔记下的所有根评论（即没有父评论的评论）
        List<CommentViewPojo> result = commentRep.findCommentViewById(note_id);

        // 获取所有根评论的子评论（回复）
        for (CommentViewPojo commentViewPojo : result) {
            commentViewPojo.setReplies(
                    commentRep.findReplyByParentId(commentViewPojo.getId())
            );
        }

        // 提取所有根评论和子评论的用户ID
        // 提取根评论的用户 ID
        Set<Long> userIds = result.stream()
                .map(CommentViewPojo::getUserId)
                .collect(Collectors.toSet());

        // 提取子评论的用户ID
        for (CommentViewPojo commentViewPojo : result) {
            for (ReplyViewPojo reply : commentViewPojo.getReplies()) {
                userIds.add(reply.getUserId());
            }
        }

        // 获取用户头像信息
        Map<Long, String> userAvatars = new HashMap<>();
        for (Long userId : userIds) {
            userAvatars.put(userId, userAvatarService.getUserAvatarUrl(userId));
        }

        // 获取用户昵称信息
        List<Object[]> userNamesList = commentRep.findUsernamesByUserIds(userIds);
        HashMap<Long, String> userNames = new HashMap<>();
        for (Object[] row : userNamesList) {
            Long userId = (Long) row[0];  // 获取 userId
            String username = (String) row[1];  // 获取 username
            userNames.put(userId, username);
        }

        // 为每个评论和回复设置用户头像和昵称
        for (CommentViewPojo commentViewPojo : result) {
            commentViewPojo.setAvatar(userAvatars.get(commentViewPojo.getUserId()));
            commentViewPojo.setUsername(userNames.get(commentViewPojo.getUserId()));
            for (ReplyViewPojo reply : commentViewPojo.getReplies()) {
                reply.setAvatar(userAvatars.get(reply.getUserId()));
                reply.setUsername(userNames.get(reply.getUserId()));
            }
        }

        return result;
    }
}
