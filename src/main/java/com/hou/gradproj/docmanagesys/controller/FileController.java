package com.hou.gradproj.docmanagesys.controller;

import com.hou.gradproj.docmanagesys.exception.BadRequestException;
import com.hou.gradproj.docmanagesys.exception.ResourceNotFoundException;
import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.payload.FileResponse;
import com.hou.gradproj.docmanagesys.payload.PagedResponse;
import com.hou.gradproj.docmanagesys.repository.FileRepository;
import com.hou.gradproj.docmanagesys.security.CurrentUser;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import com.hou.gradproj.docmanagesys.service.FileService;
import com.hou.gradproj.docmanagesys.util.AppConstants;
import com.hou.gradproj.docmanagesys.util.ModelMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    private final FileRepository fileRepository;

    @Autowired
    public FileController(FileService fileService, FileRepository fileRepository) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public PagedResponse<FileResponse> getAllUserFiles(@CurrentUser UserPrincipal currentUser,
                                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Page<File> userFiles = fileService.getFiles(currentUser, page, size, 0L);

        return getFileResponses(userFiles);
    }

    @GetMapping("/{typeId}")
    @PreAuthorize("hasRole('USER')")
    public PagedResponse<FileResponse> getUserFilesByType(@CurrentUser UserPrincipal currentUser,
                                                          @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                          @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
                                                          @PathVariable Long typeId) {
        Page<File> userFiles = fileService.getFiles(currentUser, page, size, typeId);

        return getFileResponses(userFiles);
    }

    private PagedResponse<FileResponse> getFileResponses(Page<File> files) {
        List<FileResponse> responses;
        if (files.getNumberOfElements() == 0) {
            responses = Collections.emptyList();
        } else {
            responses = files.map(ModelMapper::mapFileToFileResponse).getContent();
        }
        return new PagedResponse<>(responses, files.getNumber(), files.getSize(), files.getTotalElements(), files.getTotalPages(), files.isLast());
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public FileResponse uploadFile(@CurrentUser UserPrincipal currentUser, @RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        File file = fileService.uploadFile(currentUser, multipartFile);

        return ModelMapper.mapFileToFileResponse(file);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('USER')")
    public FileResponse deleteFile(@PathVariable Long fileId) {
        return ModelMapper.mapFileToFileResponse(fileService.deleteFile(fileId));
    }

    @GetMapping("/download/{fileId}")
    @PreAuthorize("hasRole('USER')")
    @SneakyThrows(IOException.class)
    public void downloadFile(@PathVariable Long fileId, HttpServletRequest request, HttpServletResponse response) {
        File targetFile = fileRepository.findById(fileId).orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        response.setCharacterEncoding(request.getCharacterEncoding());
        response.setContentType("application/octet-stream");

        @Cleanup
        InputStream inputStream = fileService.downloadFile(fileId);

        response.setHeader("Content-Disposition", "attachment;filename=" + targetFile.getName());

        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/{fileId}")
    @PreAuthorize("hasRole('USER')")
    public FileResponse rename(@PathVariable Long fileId, @RequestParam("newName") String newName) {
        return ModelMapper.mapFileToFileResponse(fileService.rename(fileId, newName));
    }
}
