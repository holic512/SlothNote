/**
 * 文件名: PNoteImageServiceImpl.java
 * 描述: 处理用户笔记图片上传并保存的服务实现类，支持图片压缩。
 * 作者: holic512
 * 创建日期: 2024-11-08
 * 版本: 1.0
 * 用法: 该服务实现类用于处理用户上传的图片，生成唯一的图片名称并调用图像存储服务进行保存。
 *      它返回保存成功或失败的状态，并提供已保存图片的 URL。
 */

package org.example.backend.user.note.noteImage.service.impl;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.util.file.LocalFileStorage;
import org.example.backend.user.note.noteImage.service.PUNoteImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PUNoteImageServiceImpl implements PUNoteImageService {

    private final LocalFileStorage localFileStorage;

    @Autowired
    public PUNoteImageServiceImpl(LocalFileStorage localFileStorage) {
        this.localFileStorage = localFileStorage;
    }

    @Override
    public Pair<Object, Object> saveCompressedNoteImage(String uid, MultipartFile image, String type) {
        try {
            String tag = localFileStorage.save(image, "noteImage/" + uid);
            String url = localFileStorage.resolveUrl(tag);
            return new Pair<>(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(false, null);
        }
    }
}
