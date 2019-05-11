package com.hou.gradproj.docmanagesys.service.Impl;

import com.hou.gradproj.docmanagesys.exception.BadRequestException;
import com.hou.gradproj.docmanagesys.exception.ResourceNotFoundException;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.repository.FileRepository;
import com.hou.gradproj.docmanagesys.repository.UserRepository;
import com.hou.gradproj.docmanagesys.service.AdminService;
import com.hou.gradproj.docmanagesys.util.AppConstants;
import com.hou.gradproj.docmanagesys.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

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
    @Transactional
    public Page<User> getUsers(int page, int size) {
        ValidateUtil.validatePageNumberAndSize(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "name");
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        //TODO delete all real files in fastDFS storage server
        fileRepository.deleteByCreatedBy(id);
        userRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean modifyStorageRoom(Long id, BigInteger storageRoom) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setStorageRoom(storageRoom);
        userRepository.save(user);
        return true;
    }
}
