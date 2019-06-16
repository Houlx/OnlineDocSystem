package com.hou.gradproj.docmanagesys.service;

import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;

public interface UserService {
    User changeName(UserPrincipal currentUser, String newName);

    User changePassword(UserPrincipal currentUser, String newPassword);
}
