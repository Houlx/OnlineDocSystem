package com.hou.gradproj.docmanagesys.util;

import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.model.User;
import com.hou.gradproj.docmanagesys.payload.FileResponse;
import com.hou.gradproj.docmanagesys.payload.UserProfile;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class ModelMapper {

    private static DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.CHINA)
            .withZone(ZoneId.systemDefault());

    public static FileResponse mapFileToFileResponse(File file) {


        FileResponse response = new FileResponse();

        response.setId(file.getId());
        response.setName(file.getName());
        response.setSize(file.getSize().intValue());
        response.setType(file.getType().getId());
        response.setCreatedAt(formatter.format(file.getCreatedAt()));
        response.setUpdatedAt(formatter.format(file.getUpdatedAt()));
        response.setGroupName(file.getGroupName());
        response.setRemoteFileName(file.getRemoteFileName());
        return response;
    }

    public static UserProfile mapUserToUserProfile(User user) {
        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getStorageRoom(),
                user.getAlreadyUsedRoom(),
                formatter.format(user.getCreatedAt())
        );
    }
}
