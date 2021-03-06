package com.hou.gradproj.docmanagesys.service;

import com.hou.gradproj.docmanagesys.model.File;
import com.hou.gradproj.docmanagesys.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileService {

    Page<File> getFiles(UserPrincipal currentUser, int page, int size, Long typeId);

    File uploadFile(UserPrincipal currentUser, MultipartFile file) throws IOException;

    File deleteFile(Long fileId);

    InputStream downloadFile(Long fileId);

    File rename(Long fileId, String newName);
}
