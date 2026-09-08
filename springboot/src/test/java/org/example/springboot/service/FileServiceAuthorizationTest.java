package org.example.springboot.service;

import org.example.springboot.dto.FileUploadDTO;
import org.example.springboot.entity.SysFileInfo;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.SysFileInfoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceAuthorizationTest {

    @Mock
    private SysFileInfoMapper fileInfoMapper;

    @Mock
    private BussinessFileValidationService bussinessFileValidationService;

    @InjectMocks
    private FileService fileService;

    @Test
    void deleteFileRejectsUserWhoDidNotUploadIt() {
        SysFileInfo file = new SysFileInfo();
        file.setId(7L);
        file.setUploadUserId(11L);
        when(fileInfoMapper.selectById(7L)).thenReturn(file);

        assertThrows(BusinessException.class, () -> fileService.deleteFile(7L, 22L));

        verify(fileInfoMapper, never()).deleteById(7L);
    }

    @Test
    void confirmTempFileRejectsUserWhoDidNotUploadIt() {
        SysFileInfo file = new SysFileInfo();
        file.setId(8L);
        file.setUploadUserId(11L);
        file.setIsTemp(1);
        file.setExpireTime(LocalDateTime.now().plusHours(1));
        when(fileInfoMapper.selectById(8L)).thenReturn(file);

        FileUploadDTO upload = new FileUploadDTO();
        upload.setBusinessType("AVATAR");
        upload.setBusinessId("22");

        assertThrows(BusinessException.class, () -> fileService.confirmTempFile(8L, upload, 22L));

        verify(fileInfoMapper, never()).updateById(file);
        verify(bussinessFileValidationService, never())
                .validateBusinessPermission("AVATAR", "22", 22L);
    }
}
