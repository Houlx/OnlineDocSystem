package com.hou.gradproj.docmanagesys.service.Impl;

import com.hou.gradproj.docmanagesys.exception.FileException;
import com.hou.gradproj.docmanagesys.exception.ResourceNotFoundException;
import com.hou.gradproj.docmanagesys.fastdfs.FastDFSClient;
import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.model.FileType;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.repository.FileRepository;
import com.hou.gradproj.docmanagesys.repository.FileTypeRepository;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import com.hou.gradproj.docmanagesys.service.FileService;
import com.hou.gradproj.docmanagesys.util.FileUtil;
import com.hou.gradproj.docmanagesys.util.ValidateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

@Slf4j
@Transactional
@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;

    private final UserRepository userRepository;

    private final FileTypeRepository fileTypeRepository;

    @Autowired
    public FileServiceImpl(FileRepository fileRepository, UserRepository userRepository, FileTypeRepository fileTypeRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileTypeRepository = fileTypeRepository;
    }

    /**
     * get files owned by one user by typeId, default is 0(all files)
     * get from database, no need to interact FastDFS
     *
     * @param currentUser file owner
     * @param page        page index
     * @param size        size number
     * @param typeId      id of type
     * @return files contained in page number page(int)
     */
    @Override
    @Transactional
    public Page<File> getFiles(UserPrincipal currentUser, int page, int size, Long typeId) {
        ValidateUtil.validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "name");

        if (typeId == 0) {
            return fileRepository.findByCreatedBy(currentUser.getId(), pageable);
        }

        FileType type = fileTypeRepository.findById(typeId).orElseThrow(() -> new ResourceNotFoundException("FileType", "id", typeId));

        return fileRepository.findByCreatedByAndType(currentUser.getId(), type, pageable);
    }

    /**
     * upload file into fastdfs storage client, and store file info into database,
     * set user's already used room
     *
     * @param currentUser   the currently authenticated user who upload the file
     * @param multipartFile the file to be uploaded
     * @return a custom file entity stored in database
     * @throws IOException upload failed
     */
    @Override
    public File uploadFile(UserPrincipal currentUser, MultipartFile multipartFile) throws IOException {
        //init file entity
        File file = new File();

        //get info from multipart file
        String name = multipartFile.getOriginalFilename();
        BigInteger size = BigInteger.valueOf(multipartFile.getSize());
        Long typeId = FileUtil.getTypeOfUploadedFile(multipartFile);
        FileType type = fileTypeRepository.findById(typeId).orElseThrow(() ->
                new ResourceNotFoundException("FileType", "id", typeId));

        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        //file name is not duplicated, and storage room is not full
        if (!fileRepository.existsByNameAndCreatedBy(name, currentUser.getId())
                && user.getAlreadyUsedRoom().add(size).compareTo(user.getStorageRoom()) <= 0) {
            //begin upload into FastDFS storage server
            String[] groupNameAndRemoteFileName = FastDFSClient.uploadFile(multipartFile, currentUser.getUsername());
            //if upload success, [groupName, remoteFileName] can't be null
            //set file entity
            if (groupNameAndRemoteFileName != null) {
                file.setGroupName(groupNameAndRemoteFileName[0]);
                file.setRemoteFileName(groupNameAndRemoteFileName[1]);
                file.setName(name);
                file.setSize(size);
                file.setType(type);

                //save file info to database
                File result = fileRepository.save(file);

                //add file size to user's already used storage room and save user
                user.setAlreadyUsedRoom(user.getAlreadyUsedRoom().add(size));
                userRepository.save(user);

                return result;
            }
        } else if (fileRepository.existsByNameAndCreatedBy(name, currentUser.getId())) {
            throw new FileException("File already exists.");
        } else if (user.getAlreadyUsedRoom().add(size).compareTo(user.getStorageRoom()) > 0) {
            throw new FileException("Storage full.");
        } else {
            throw new FileException("Internal Error");
        }
        return null;
    }

    /**
     * delete file from database and fastdfs storage client,
     * set user's already used room
     *
     * @param id file id
     * @return file entity with info
     */
    @Override
    @SneakyThrows(Exception.class)
    public File deleteFile(Long id) {
        //find the file from database
        File deleteFile = fileRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("File", "id", id));

        //find file owner
        User owner = userRepository.findById(deleteFile.getCreatedBy()).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", deleteFile.getCreatedBy()));

        //if delete file in FastDFS storage server success (return 0 for success)
        if (FastDFSClient.deleteFile(deleteFile.getGroupName(), deleteFile.getRemoteFileName()) == 0) {
            //delete file info from database
            fileRepository.deleteById(id);

            //user's already used storage room subtracts file size and save user
            owner.setAlreadyUsedRoom(owner.getAlreadyUsedRoom().subtract(deleteFile.getSize()));
            userRepository.save(owner);
            return deleteFile;
        }
        return null;
    }

    /**
     * download file by file id
     *
     * @param id file id
     * @return InputStream contains file bytes
     */
    @Override
    public InputStream downloadFile(Long id) {
        File downloadFile = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        return FastDFSClient.downFile(downloadFile.getGroupName(), downloadFile.getRemoteFileName());
    }

    /**
     * rename file (only in database)
     *
     * @param id      file id
     * @param newName file's new name
     * @return true if rename successfully, otherwise false
     */
    @Override
    public boolean rename(Long id, String newName) {
        File target = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        String ext = target.getName().substring(target.getName().lastIndexOf("."));

        target.setName(newName + ext);
        fileRepository.save(target);
        return true;
    }
}
