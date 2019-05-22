package com.hou.gradproj.docmanagesys.fastdfs;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class FastDFSClient {

    static {
        try {
            String filePath = new ClassPathResource("config/fastdfs_conf.properties").getFile().getAbsolutePath();
            ClientGlobal.initByProperties(filePath);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        log.warn(ClientGlobal.g_secret_key);
    }

    //return [groupName, remoteFileName]
    public static String[] uploadFile(MultipartFile multipartFile, String username) throws IOException {
        String[] fileAbsolutePath;
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

        fileAbsolutePath = FastDFSClient.upload(file);  //upload to fastdfs

        if (fileAbsolutePath == null) {
            log.error("upload file failed,please upload again!");
            throw new NullPointerException("file absolute path is null.");
        }
        return fileAbsolutePath;
    }

    @SneakyThrows({IOException.class, MyException.class})
    public static FileInfo getFile(String groupName, String remoteFileName) {
        StorageClient storageClient = getTrackerClient();
        return storageClient.get_file_info(groupName, remoteFileName);
    }

    @SneakyThrows({IOException.class, MyException.class})
    public static InputStream downFile(String groupName, String remoteFileName) {
        StorageClient storageClient = getTrackerClient();
        byte[] fileByte = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(fileByte);
    }

    //return 0 for success, non-zero for fail
    public static int deleteFile(String groupName, String remoteFileName)
            throws Exception {
        StorageClient storageClient = getTrackerClient();
        return storageClient.delete_file(groupName, remoteFileName);
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
    @SneakyThrows({IOException.class, MyException.class})
    private static String[] upload(FastDFSFile file) {
        log.info("File Name: " + file.getName() + "File Length:" + file.getContent().length);

        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", file.getAuthor());

        long startTime = System.currentTimeMillis();

        StorageClient storageClient = getTrackerClient();
        String[] uploadResults = storageClient.upload_file(file.getContent(), file.getExt(), meta_list);

        log.info("upload_file time used:" + (System.currentTimeMillis() - startTime) + " ms");

        if (uploadResults == null) {
            log.error("upload file fail, error code:" + storageClient.getErrorCode());
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
