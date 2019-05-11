package com.hou.gradproj.docmanagesys.service.Impl;

import com.hou.gradproj.docmanagesys.exception.BadRequestException;
import com.hou.gradproj.docmanagesys.exception.FileException;
import com.hou.gradproj.docmanagesys.exception.ResourceNotFoundException;
import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.model.FileType;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.repository.FileRepository;
import com.hou.gradproj.docmanagesys.repository.FileTypeRepository;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import com.hou.gradproj.docmanagesys.service.FileService;
import com.hou.gradproj.docmanagesys.util.AppConstants;
import com.hou.gradproj.docmanagesys.util.FileUtil;
import com.hou.gradproj.docmanagesys.util.ValidateUtil;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;

    private final UserRepository userRepository;

    private final FileTypeRepository fileTypeRepository;

    @Value("${app.save_file_path}")
    private String SAVE_FILE_PATH;

    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    public FileServiceImpl(FileRepository fileRepository, UserRepository userRepository, FileTypeRepository fileTypeRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileTypeRepository = fileTypeRepository;
    }

    /**
     * get files owned by one user by typeId, default is 0(all files)
     *
     * @param currentUser file owner
     * @param page        page index
     * @param size        size number
     * @param typeId      id of type
     * @return files in page
     */
    @Override
    @Transactional
    public Page<File> getFiles(UserPrincipal currentUser, int page, int size, Long typeId) {
        ValidateUtil.validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "name");

        if (typeId == 0L) {
            return fileRepository.findByCreatedBy(currentUser.getId(), pageable);
        }

        FileType type = fileTypeRepository.findById(typeId).orElseThrow(() -> new ResourceNotFoundException("FileType", "id", typeId));

        return fileRepository.findByCreatedByAndType(currentUser.getId(), type, pageable);
    }

    /**
     * handle upload file
     *
     * @param currentUser   the currently authenticated user who upload the file
     * @param multipartFile the file to be uploaded
     * @return a customized file entity
     * @throws IOException upload failed
     */
    @Override
    @Transactional
    public File uploadFile(UserPrincipal currentUser, MultipartFile multipartFile) throws IOException {
        File file = new File();

        String name = multipartFile.getOriginalFilename();
        BigInteger size = BigInteger.valueOf(multipartFile.getSize());
        Long typeId = FileUtil.getTypeOfUploadedFile(multipartFile);
        FileType type = fileTypeRepository.findById(typeId).orElseThrow(() ->
                new ResourceNotFoundException("FileType", "id", typeId));
        String dirPath = SAVE_FILE_PATH + currentUser.getId();

        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        java.io.File dest = new java.io.File(dirPath);
        String path = null;
        if (!dest.exists()) {
            if (dest.mkdirs()) {
                path = dirPath + "/" + name;
            }
        }

        if (!fileRepository.existsByNameAndCreatedBy(name, currentUser.getId())
                && user.getAlreadyUsedRoom().add(size).compareTo(user.getStorageRoom()) < 0
                && path != null) {
            multipartFile.transferTo(new java.io.File(path));
            file.setName(name);
            file.setSize(size);
            file.setType(type);
            file.setPath(dirPath);
            fileRepository.save(file);

            user.setAlreadyUsedRoom(user.getAlreadyUsedRoom().add(size));
            userRepository.save(user);
            return file;
        } else {
            throw new FileException("File already exists");
        }
    }

    /**
     * handle delete file
     *
     * @param id file id
     * @return file entity with info
     */
    @Override
    @Transactional
    public File deleteFile(Long id) {
        File deleteFile = fileRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("File", "id", id));

        User owner = userRepository.findById(deleteFile.getCreatedBy()).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", deleteFile.getCreatedBy()));

        java.io.File file = new java.io.File(deleteFile.getPath() + "/" + deleteFile.getName());
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                fileRepository.deleteById(id);
                owner.setAlreadyUsedRoom(owner.getAlreadyUsedRoom().subtract(deleteFile.getSize()));
                userRepository.save(owner);
                return deleteFile;
            }
        }
        return null;
    }

    /**
     * load file from storage into a Resource object
     *
     * @param id file id
     * @return Resource containing file
     */
    @Override
    @Transactional
    @SneakyThrows(MalformedURLException.class)
    public Resource loadFileAsResource(Long id) {
        File downloadFile = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        Path filePath = Paths.get(downloadFile.getPath() + "/" + downloadFile.getName()).toAbsolutePath().normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        }
        return null;
    }

    /**
     * rename file
     *
     * @param id      file id
     * @param newName file's new name
     * @return true if rename successfully, otherwise false
     */
    @Override
    @Transactional
    public boolean rename(Long id, String newName) {
        File target = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        java.io.File oldFile = new java.io.File(target.getPath() + "/" + target.getName());
        java.io.File newFile = new java.io.File(target.getPath() + "/" + newName);
        if (oldFile.renameTo(newFile)) {
            target.setName(newName);
            fileRepository.save(target);
            return true;
        } else {
            return false;
        }
    }
}
