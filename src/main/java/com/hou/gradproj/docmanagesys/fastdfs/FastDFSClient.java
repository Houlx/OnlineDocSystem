package com.hou.gradproj.docmanagesys.fastdfs;

import lombok.Cleanup;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FastDFSClient {
    private static Logger logger = LoggerFactory.getLogger(FastDFSClient.class);

    static {
        try {
            String filePath = new ClassPathResource("config/fastdfs_conf.properties").getFile().getAbsolutePath();
            ClientGlobal.initByProperties(filePath);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
    }

    //return file resource url
    public static String uploadFile(MultipartFile multipartFile, String username) throws IOException {
        String[] fileAbsolutePath = {};
        String fileName = multipartFile.getOriginalFilename();
        String ext = null;
        if (fileName != null) {
            ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        byte[] file_buff = null;

        @Cleanup
        InputStream inputStream = multipartFile.getInputStream();

        if (inputStream != null) {
            int len1 = inputStream.available();
            file_buff = new byte[len1];
            inputStream.read(file_buff);
        }
        FastDFSFile file = new FastDFSFile(fileName, file_buff, ext, username);

        try {
            fileAbsolutePath = FastDFSClient.upload(file);  //upload to fastdfs
        } catch (Exception e) {
            logger.error("upload file Exception!", e);
        }
        if (fileAbsolutePath == null) {
            logger.error("upload file failed,please upload again!");
            throw new NullPointerException("file absolute path is null.");
        }
        return FastDFSClient.getTrackerUrl() + fileAbsolutePath[0] + "/" + fileAbsolutePath[1];
    }

    public static FileInfo getFile(String groupName, String remoteFileName) {
        try {
            StorageClient storageClient = getTrackerClient();
            return storageClient.get_file_info(groupName, remoteFileName);
        } catch (IOException e) {
            logger.error("IO Exception: Get File from Fast DFS failed", e);
        } catch (Exception e) {
            logger.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return null;
    }

    public static InputStream downFile(String groupName, String remoteFileName) {
        try {
            StorageClient storageClient = getTrackerClient();
            byte[] fileByte = storageClient.download_file(groupName, remoteFileName);
            return new ByteArrayInputStream(fileByte);
        } catch (IOException e) {
            logger.error("IO Exception: Get File from Fast DFS failed", e);
        } catch (Exception e) {
            logger.error("Non IO Exception: Get File from Fast DFS failed", e);
        }
        return null;
    }

    //return 0 for success, non-zero for fail
    public static int deleteFile(String groupName, String remoteFileName)
            throws Exception {
        StorageClient storageClient = getTrackerClient();
        return storageClient.delete_file(groupName, remoteFileName);
//        logger.info("delete file successfully!!!" + i);
    }

    public static StorageServer[] getStoreStorages(String groupName)
            throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getStoreStorages(trackerServer, groupName);
    }

    public static ServerInfo[] getFetchStorages(String groupName,
                                                String remoteFileName) throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

    public static String getTrackerUrl() throws IOException {
        return "http://" + getTrackerServer().getInetSocketAddress().getHostString() + ":" + ClientGlobal.getG_tracker_http_port() + "/";
    }

    //return [groupName, remoteFileName]: String[]
    private static String[] upload(FastDFSFile file) {
        logger.info("File Name: " + file.getName() + "File Length:" + file.getContent().length);

        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", file.getAuthor());

        long startTime = System.currentTimeMillis();
        String[] uploadResults = null;
        StorageClient storageClient = null;
        try {
            storageClient = getTrackerClient();
            uploadResults = storageClient.upload_file(file.getContent(), file.getExt(), meta_list);
        } catch (IOException e) {
            logger.error("IO Exception when uploading the file:" + file.getName(), e);
        } catch (Exception e) {
            logger.error("Non IO Exception when uploading the file:" + file.getName(), e);
        }
        logger.info("upload_file time used:" + (System.currentTimeMillis() - startTime) + " ms");

        if (uploadResults == null && storageClient != null) {
            logger.error("upload file fail, error code:" + storageClient.getErrorCode());
        }

        return uploadResults;
    }

    private static StorageClient getTrackerClient() throws IOException {
        TrackerServer trackerServer = getTrackerServer();
        return new StorageClient(trackerServer, null);
    }

    private static TrackerServer getTrackerServer() throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        return trackerClient.getConnection();
    }
}
