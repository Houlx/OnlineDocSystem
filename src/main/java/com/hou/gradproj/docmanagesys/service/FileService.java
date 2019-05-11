package com.hou.gradproj.docmanagesys.service;

import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    Page<File> getFiles(UserPrincipal currentUser, int page, int size, Long typeId);

    File uploadFile(UserPrincipal currentUser, MultipartFile file) throws IOException;

    File deleteFile(Long fileId);

    Resource loadFileAsResource(Long fileId);

    boolean rename(Long fileId, String newName);
}
