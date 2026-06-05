package com.yatidle.backend.util;

import com.yatidle.backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageUploadValidatorTest {

    @Test
    void acceptsCommonImageTypesAndReturnsSafeExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.PNG", "image/png", new byte[]{1, 2, 3});

        String ext = ImageUploadValidator.validate(file);

        assertThat(ext).isEqualTo(".png");
    }

    @Test
    void rejectsNonImageContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> ImageUploadValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能上传图片文件");
    }

    @Test
    void rejectsOversizedImage() {
        byte[] content = new byte[(int) ImageUploadValidator.MAX_IMAGE_SIZE_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", content);

        assertThatThrownBy(() -> ImageUploadValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能超过");
    }
}
