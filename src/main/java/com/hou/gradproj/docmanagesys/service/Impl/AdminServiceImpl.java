package com.hou.gradproj.docmanagesys.service.Impl;

import com.hou.gradproj.docmanagesys.exception.ResourceNotFoundException;
import com.hou.gradproj.docmanagesys.fastdfs.FastDFSClient;
import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.repository.FileRepository;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.service.AdminService;
import com.hou.gradproj.docmanagesys.util.ValidateUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Transactional
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    private final FileRepository fileRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, FileRepository fileRepository) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    public Page<User> getUsers(int page, int size) {
        ValidateUtil.validatePageNumberAndSize(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "name");
        return userRepository.findAll(pageable);
    }

    @Override
    @SneakyThrows(Exception.class)
    public boolean deleteUser(Long id) {
        List<File> filesToDel = fileRepository.findByCreatedBy(id);
        for (File file : filesToDel) {
            FastDFSClient.deleteFile(file.getGroupName(), file.getRemoteFileName());
        }
        fileRepository.deleteByCreatedBy(id);
        userRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean modifyStorageRoom(Long id, BigInteger storageRoom) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setStorageRoom(storageRoom);
        userRepository.save(user);
        return true;
    }
}
