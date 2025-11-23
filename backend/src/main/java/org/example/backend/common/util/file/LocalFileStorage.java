package org.example.backend.common.util.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LocalFileStorage {

    private final LocalStorageProperties props;

    @Autowired
    public LocalFileStorage(LocalStorageProperties props) {
        this.props = props;
    }

    public String save(MultipartFile file, String subDir) throws IOException {
        String baseDir = normalizeDir(props.getRootDir());
        Path dir = Paths.get(baseDir);
        if (subDir != null && !subDir.isBlank()) {
            dir = dir.resolve(safePath(subDir));
        }
        if (Boolean.TRUE.equals(props.getEnableDatePath())) {
            LocalDate d = LocalDate.now();
            dir = dir.resolve(String.valueOf(d.getYear()))
                     .resolve(String.format("%02d", d.getMonthValue()))
                     .resolve(String.format("%02d", d.getDayOfMonth()));
        }
        Files.createDirectories(dir);

        String original = Objects.toString(file.getOriginalFilename(), "");
        String ext = extractExtension(original);
        String name = buildRandomName(ext, Boolean.TRUE.equals(props.getKeepExtension()));
        Path target = dir.resolve(name);
        Files.copy(file.getInputStream(), target);

        Path base = Paths.get(normalizeDir(props.getRootDir()));
        String tag = base.relativize(target).toString().replace('\\', '/');
        return tag;
    }

    public String resolveUrl(String tag) {
        String baseUrl = Objects.toString(props.getPublicBaseUrl(), "");
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        tag = tag.startsWith("/") ? tag.substring(1) : tag;
        return baseUrl + "/" + tag;
    }

    private String buildRandomName(String ext, boolean keepExt) {
        long ts = System.currentTimeMillis();
        int rnd = ThreadLocalRandom.current().nextInt(1000, 10000);
        String base = ts + "-" + rnd;
        if (keepExt && ext != null && !ext.isBlank()) {
            return base + "." + ext;
        }
        return base;
    }

    private String extractExtension(String filename) {
        if (filename == null) return null;
        int idx = filename.lastIndexOf('.');
        if (idx <= 0 || idx == filename.length() - 1) return null;
        return filename.substring(idx + 1);
    }

    private String normalizeDir(String dir) {
        if (dir == null) return "";
        return dir.replace('\\', '/');
    }

    private String safePath(String p) {
        String s = p.replace('\\', '/');
        while (s.startsWith("/")) s = s.substring(1);
        return s;
    }
}