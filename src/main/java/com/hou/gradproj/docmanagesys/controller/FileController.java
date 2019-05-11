package com.hou.gradproj.docmanagesys.controller;

import com.hou.gradproj.docmanagesys.exception.BadRequestException;
import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.payload.ApiResponse;
import com.hou.gradproj.docmanagesys.payload.FileResponse;
import com.hou.gradproj.docmanagesys.payload.PagedResponse;
import com.hou.gradproj.docmanagesys.security.CurrentUser;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import com.hou.gradproj.docmanagesys.service.FileService;
import com.hou.gradproj.docmanagesys.util.AppConstants;
import com.hou.gradproj.docmanagesys.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
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
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId, HttpServletRequest request) {
        Resource resource = fileService.loadFileAsResource(fileId);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION
                        , "attachment;filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/{fileId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> rename(@PathVariable Long fileId, @RequestParam("newName") String newName) {
        if (fileService.rename(fileId, newName)) {
            return ResponseEntity.ok(new ApiResponse(true, "Renamed successfully"));
        }
        return new ResponseEntity<>(new ApiResponse(false, "Rename failed"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
