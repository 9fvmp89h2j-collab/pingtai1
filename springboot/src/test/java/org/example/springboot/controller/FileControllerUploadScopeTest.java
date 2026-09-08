package org.example.springboot.controller;

import org.example.springboot.dto.response.UserDetailResponseDTO;
import org.example.springboot.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileControllerUploadScopeTest {

    private final FileController controller = new FileController();

    @Test
    void ordinaryUserCanUploadOnlyOwnAvatar() {
        UserDetailResponseDTO user = user(12L, "USER");

        assertDoesNotThrow(() -> controller.validateUploadScope(user, "USER_AVATAR", "12"));
        assertThrows(BusinessException.class,
                () -> controller.validateUploadScope(user, "USER_AVATAR", "99"));
    }

    @Test
    void ordinaryUserPostUploadMustUseOwnedNamespace() {
        UserDetailResponseDTO user = user(12L, "USER");

        assertDoesNotThrow(() -> controller.validateUploadScope(
                user, "POST_CONTENT", "post-12-12345678-abcd"));
        assertThrows(BusinessException.class,
                () -> controller.validateUploadScope(user, "POST_CONTENT", "post-99-12345678-abcd"));
        assertThrows(BusinessException.class,
                () -> controller.validateUploadScope(user, "COURSE", "12"));
    }

    @Test
    void administratorCanUseManagedBusinessTypes() {
        UserDetailResponseDTO admin = user(1L, "ADMIN");

        assertDoesNotThrow(() -> controller.validateUploadScope(admin, "COURSE", "course-1"));
    }

    private UserDetailResponseDTO user(Long id, String type) {
        UserDetailResponseDTO dto = new UserDetailResponseDTO();
        dto.setId(id);
        dto.setUserType(type);
        return dto;
    }
}
