package com.hou.gradproj.docmanagesys.service;

import com.hou.gradproj.docmanagesys.model.User;
import org.springframework.data.domain.Page;

import java.math.BigInteger;

public interface AdminService {
    Page<User> getUsers(int page, int size);

    boolean deleteUser(Long id);

    boolean modifyStorageRoom(Long id, BigInteger storageRoom);
}
