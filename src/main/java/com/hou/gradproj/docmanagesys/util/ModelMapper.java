package com.hou.gradproj.docmanagesys.util;

import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.payload.FileResponse;
import com.hou.gradproj.docmanagesys.payload.UserProfile;

public class ModelMapper {

    public static FileResponse mapFileToFileResponse(File file) {
        FileResponse response = new FileResponse();

        response.setId(file.getId());
        response.setName(file.getName());
        response.setPath(file.getPath());
        response.setSize(file.getSize().intValue());
        response.setType(file.getType().getId());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());

        return response;
    }

    public static UserProfile mapUserToUserProfile(User user) {
        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getStorageRoom(),
                user.getAlreadyUsedRoom(),
                user.getCreatedAt()
        );
    }
}
