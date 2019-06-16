package com.hou.gradproj.docmanagesys.util;

import lombok.SneakyThrows;
import org.csource.fastdfs.ProtoCommon;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

public class FileUtil {
    public static Long getTypeOfUploadedFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.length() > 0) {
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            switch (suffix) {
                case "txt":
                    return 1L;
                case "doc":
                case "docx":
                    return 2L;
                case "ppt":
                case "pptx":
                    return 3L;
                case "xls":
                case "xlsx":
                    return 4L;
                case "pdf":
                    return 5L;
                default:
                    return 6L;
            }
        }
        return null;
    }

    @SneakyThrows
    public static String getToken(String remoteFileName, String httpSecretKey) {
        int ts = (int) Instant.now().getEpochSecond();

        String token = ProtoCommon.getToken(remoteFileName, ts, httpSecretKey);

        return "token=" + token +
                "&ts=" + ts;
    }
}
