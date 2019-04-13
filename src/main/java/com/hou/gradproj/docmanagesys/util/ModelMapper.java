package com.hou.gradproj.docmanagesys.util;

import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.payload.FileResponse;

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
}
