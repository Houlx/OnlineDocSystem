package com.hou.gradproj.docmanagesys.controller;

import com.hou.gradproj.docmanagesys.exception.BadRequestException;
import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.payload.ApiResponse;
import com.hou.gradproj.docmanagesys.payload.FileResponse;
import com.hou.gradproj.docmanagesys.payload.PagedResponse;
import com.hou.gradproj.docmanagesys.payload.UserSummary;
import com.hou.gradproj.docmanagesys.repository.FileRepository;
import com.hou.gradproj.docmanagesys.repository.FileTypeRepository;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.security.CurrentUser;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import com.hou.gradproj.docmanagesys.service.FileService;
import com.hou.gradproj.docmanagesys.util.AppConstants;
import com.hou.gradproj.docmanagesys.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final UserRepository userRepository;

    private final FileRepository fileRepository;

    private final FileTypeRepository fileTypeRepository;

    private final FileService fileService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    public FileController(UserRepository userRepository, FileRepository fileRepository, FileTypeRepository fileTypeRepository, FileService fileService) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.fileTypeRepository = fileTypeRepository;
        this.fileService = fileService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public PagedResponse<FileResponse> getFiles(@CurrentUser UserPrincipal currentUser,
                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return fileService.getFiles(currentUser, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public FileResponse uploadFile(@CurrentUser UserPrincipal currentUser, @RequestParam(value = "file") MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        File file = fileService.uploadFile(currentUser, multipartFile);

        FileResponse response = ModelMapper.mapFileToFileResponse(file);
        response.setCreatedBy(new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName()));

        return response;
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('USER')")
    public FileResponse deleteFile(@PathVariable Long fileId) {
        return fileService.deleteFile(fileId);
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

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
