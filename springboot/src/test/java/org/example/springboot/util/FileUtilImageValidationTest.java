package org.example.springboot.util;

import org.example.springboot.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUtilImageValidationTest {

    @Test
    void acceptsMatchingPngSignature() {
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "safe.png", "image/png", png);

        assertDoesNotThrow(() -> FileUtil.validateImageSignature(file, ".png"));
    }

    @Test
    void rejectsHtmlDisguisedAsImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "unsafe.jpg",
                "image/jpeg",
                "<script>alert(1)</script>".getBytes()
        );

        assertThrows(BusinessException.class, () -> FileUtil.validateImageSignature(file, ".jpg"));
    }

    @Test
    void rejectsNonImageInBusinessDirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "certificate.mp4",
                "video/mp4",
                new byte[] {0, 0, 0, 0}
        );

        assertThrows(BusinessException.class,
                () -> FileUtil.saveFile(file, "bussiness", "user_avatar"));
    }
}
