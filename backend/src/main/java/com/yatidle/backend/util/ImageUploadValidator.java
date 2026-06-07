package com.yatidle.backend.util;

import com.yatidle.backend.common.exception.BusinessException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ImageUploadValidator {
    public static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private ImageUploadValidator() {
    }

    public static String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("图片文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException("图片大小不能超过 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !EXTENSIONS_BY_CONTENT_TYPE.containsKey(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("只能上传图片文件");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = extension(originalFilename);
        if (ext.isEmpty()) {
            return EXTENSIONS_BY_CONTENT_TYPE.get(contentType.toLowerCase(Locale.ROOT));
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("图片格式仅支持 jpg、png、webp、gif");
        }
        return ".jpeg".equals(ext) ? ".jpg" : ext;
    }

    private static String extension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
