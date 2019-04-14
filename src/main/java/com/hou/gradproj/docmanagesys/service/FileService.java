package com.hou.gradproj.docmanagesys.service;

import com.hou.gradproj.docmanagesys.exception.BadRequestException;
import com.hou.gradproj.docmanagesys.exception.FileException;
import com.hou.gradproj.docmanagesys.exception.ResourceNotFoundException;
import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.model.FileType;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.payload.FileResponse;
import com.hou.gradproj.docmanagesys.payload.PagedResponse;
import com.hou.gradproj.docmanagesys.payload.UserSummary;
import com.hou.gradproj.docmanagesys.repository.FileRepository;
import com.hou.gradproj.docmanagesys.repository.FileTypeRepository;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import com.hou.gradproj.docmanagesys.util.AppConstants;
import com.hou.gradproj.docmanagesys.util.FileUtil;
import com.hou.gradproj.docmanagesys.util.ModelMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Service
public class FileService {
    private final FileRepository fileRepository;

    private final UserRepository userRepository;

    private final FileTypeRepository fileTypeRepository;

    @Value("${app.save_file_path}")
    private String SAVE_FILE_PATH;

    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    public FileService(FileRepository fileRepository, UserRepository userRepository, FileTypeRepository fileTypeRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileTypeRepository = fileTypeRepository;
    }

    /**
     * Get All Files Owned by One User
     *
     * @param currentUser the user currently authenticated
     * @param page        number of page to return
     * @param size        size number of one page
     * @return a paged response composed by several file responses
     */
    public PagedResponse<FileResponse> getFiles(UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "name");
        Page<File> files = fileRepository.findByCreatedBy(currentUser.getId(), pageable);
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        UserSummary userSummary = new UserSummary(user.getId(), user.getUsername(), user.getName());

        if (files.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), files.getNumber(),
                    files.getSize(), files.getTotalElements(), files.getTotalPages(), files.isLast());
        }

        List<FileResponse> fileResponses = files.map(ModelMapper::mapFileToFileResponse)
                .getContent();

        for (FileResponse response : fileResponses) {
            response.setCreatedBy(userSummary);
        }

        return new PagedResponse<>(fileResponses, files.getNumber(), files.getSize(), files.getTotalElements(), files.getTotalPages(), files.isLast());
    }

    /**
     * handle upload file
     *
     * @param currentUser   the currently authenticated user who upload the file
     * @param multipartFile the file to be uploaded
     * @return a customized file entity
     * @throws IOException upload failed
     */
    public File uploadFile(UserPrincipal currentUser, MultipartFile multipartFile) throws IOException {
        File file = new File();

        String name = multipartFile.getOriginalFilename();
        BigInteger size = BigInteger.valueOf(multipartFile.getSize());
        Long typeId = FileUtil.getTypeOfUploadedFile(multipartFile);
        FileType type = fileTypeRepository.findById(typeId).orElseThrow(() ->
                new ResourceNotFoundException("FileType", "id", typeId));
        String dirPath = SAVE_FILE_PATH + currentUser.getId();

        java.io.File dest = new java.io.File(dirPath);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        String path = dirPath + "/" + name;

        if (!fileRepository.existsByNameAndCreatedBy(name, currentUser.getId())) {
            multipartFile.transferTo(new java.io.File(path));
            file.setName(name);
            file.setSize(size);
            file.setType(type);
            file.setPath(path);
            fileRepository.save(file);
            return file;
        } else {
            throw new FileException("File already exists");
        }
    }

    public FileResponse deleteFile(Long id) {
        File deleteFile = fileRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("File", "id", id));

        FileResponse response = ModelMapper.mapFileToFileResponse(deleteFile);
        User owner = userRepository.findById(deleteFile.getCreatedBy()).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", deleteFile.getCreatedBy()));
        response.setCreatedBy(new UserSummary(owner.getId(), owner.getUsername(), owner.getName()));

        java.io.File file = new java.io.File(deleteFile.getPath());
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                fileRepository.deleteById(id);
                return response;
            }
        }
        return null;
    }

    @SneakyThrows(MalformedURLException.class)
    public Resource loadFileAsResource(Long id) {
        File downloadFile = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        Path filePath = Paths.get(downloadFile.getPath()).toAbsolutePath().normalize();
//        logger.warn(filePath.toUri().toString());
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        }
        return null;
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}
