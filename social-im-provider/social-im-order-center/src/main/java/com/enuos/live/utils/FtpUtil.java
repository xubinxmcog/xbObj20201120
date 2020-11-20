//package com.enuos.live.utils;
//
//import cn.hutool.core.date.DateTime;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.net.ftp.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Random;
//
///**
// * @ClassName FtpUtil
// * @Description: TODO
// * @Author xubin
// * @Date 2020/4/10
// * @Version V1.0
// **/
//@Slf4j
////@Component
//public class FtpUtil {
//
//    @Value("${ftp.host}")
//    private String host;
//
//    @Value("${ftp.port}")
//    private int port;
//
//    @Value("${ftp.username}")
//    private String userName;
//
//    @Value("${ftp.password}")
//    private String password;
//
//    @Value("${ftp.basePath}")
//    private String basePath;
//
//    @Value("${ftp.httpPath}")
//    private String httpPath;
//
//    @Value("${ftp.filepath}")
//    private String filePath;
//
//    public static final String DIRSPLIT = "/";
//
//    // ftp客户端
//    private FTPClient ftpClient = new FTPClient();
//
//    /**
//     * @MethodName: uploadFile
//     * @Description: TODO 上传文件
//     * @Param: [host, port, username, password, basePath, filePath, fileName, inputStream]
//     * @Return: java.lang.Boolean
//     * @Author: xubin
//     * @Date: 2020/4/10
//     **/
//    public Boolean uploadFile(String fileName, InputStream inputStream) throws Exception {
//        //创建FTPClient对象（对于连接ftp服务器，以及上传和上传都必须要用到一个对象）
////            //1、创建临时路径
////            String tempPath = "";
////            //2、创建FTPClient对象（对于连接ftp服务器，以及上传和上传都必须要用到一个对象）
////            FTPClient ftp = new FTPClient();
//        try {
////            FTPClient ftpClient = new FTPClient();
//            //3、定义返回的状态码
//            int reply;
//            //4、连接ftp(当前项目所部署的服务器和ftp服务器之间可以相互通讯，表示连接成功)
//            ftpClient.connect(host, port);
//            //5、输入账号和密码进行登录
//            ftpClient.login(userName, password);
//            //6、接受状态码(如果成功，返回230，如果失败返回503)
//            reply = ftpClient.getReplyCode();
//            log.info("ftp连接状态码：" + reply);
//            //7、根据状态码检测ftp的连接，调用isPositiveCompletion(reply)-->如果连接成功返回true，否则返回false
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                //说明连接失败，需要断开连接
//                ftpClient.disconnect();
//                log.info("连接失败");
//                return false;
//            }
//            log.info("连接成功！");
//            //8.把文件转换为二进制字符流的形式进行上传
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            //9.修改操作空间
//            ftpClient.changeWorkingDirectory(basePath);
//            //17、上传方法storeFile(filename,input),返回Boolean雷类型，上传成功返回true
//            if (!ftpClient.storeFile(fileName, inputStream)) {
//                return false;
//            }
//            // 18.关闭输入流
//            inputStream.close();
//            // 19.退出ftp
//            ftpClient.logout();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new IOException(e);
//        } finally {
//            if (ftpClient.isConnected()) {
//                try {
//                    // 20.断开ftp的连接
//                    ftpClient.disconnect();
//                } catch (IOException ioe) {
//                    ioe.printStackTrace();
//                }
//            }
//            return true;
//        }
//    }
//
//    /**
//     * @MethodName: downloadFile
//     * @Description: TODO  下载文件
//     * @Param: [pathname, filename, localpath]
//     * @Return: boolean
//     * @Author: xubin
//     * @Date: 2020/4/10
//     **/
//    public InputStream downloadFile(String fileName) throws FTPConnectionClosedException {
//        InputStream in = null;
//        try {
//
//            // 建立连接
//            connectToServer();
//            ftpClient.enterLocalPassiveMode();
//            // 设置传输二进制文件
//            setFileType(FTP.BINARY_FILE_TYPE);
//            int reply = ftpClient.getReplyCode();
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                ftpClient.disconnect();
//                throw new IOException("failed to connect to the FTP Server:" + host);
//            }
//            ftpClient.changeWorkingDirectory(basePath);
//
//            // ftp文件获取文件
//            in = ftpClient.retrieveFileStream(fileName);
//
//        } catch (FTPConnectionClosedException e) {
//            log.error("ftp连接被关闭！", e);
//            throw e;
//        } catch (Exception e) {
//            log.error("ERR : upload file " + fileName + " from ftp : failed!", e);
//        }
//        return in;
//
//    }
//
//    /**
//     * @MethodName: connectToServer
//     * @Description: TODO 连接到ftp服务器
//     * @Param: []
//     * @Return: void
//     * @Author: xubin
//     * @Date: 2020/4/10
//     **/
//    private void connectToServer() throws Exception {
//        if (!ftpClient.isConnected()) {
//            int reply;
//            try {
//                ftpClient = new FTPClient();
//                ftpClient.connect(host, port);
//                ftpClient.login(userName, password);
//                reply = ftpClient.getReplyCode();
//
//                if (!FTPReply.isPositiveCompletion(reply)) {
//                    ftpClient.disconnect();
//                    log.info("connectToServer FTP server refused connection.");
//                }
//
//            } catch (FTPConnectionClosedException ex) {
//                log.error("服务器:IP：" + host + "没有连接数！there are too many connected users,please try later", ex);
//                throw ex;
//            } catch (Exception e) {
//                log.error("登录ftp服务器【" + host + "】失败", e);
//                throw e;
//            }
//        }
//    }
//
//    /**
//     * @MethodName: setFileType
//     * @Description: TODO 设置传输文件的类型[文本文件或者二进制文件
//     * @Param: [fileType]
//     * @Return: void
//     * @Author: xubin
//     * @Date: 2020/4/10
//     **/
//    private void setFileType(int fileType) {
//        try {
//            ftpClient.setFileType(fileType);
//        } catch (Exception e) {
//            log.error("ftp设置传输文件的类型时失败！", e);
//        }
//    }
//
//    /**
//     * @MethodName: closeConnect
//     * @Description: TODO 功能：关闭连接
//     * @Param: []
//     * @Return: void
//     * @Author: xubin
//     * @Date: 2020/4/10
//     **/
//    public void closeConnect() {
//        try {
//            if (ftpClient != null) {
//                ftpClient.logout();
//                ftpClient.disconnect();
//            }
//        } catch (Exception e) {
//            log.error("ftp连接关闭失败！", e);
//        }
//    }
//
//
//    /**
//     * @MethodName: getFileName
//     * @Description: TODO 返回文件名
//     * @Param: []
//     * @Return: java.lang.String
//     * @Author: xubin
//     * @Date: 2020/4/10
//     **/
//    public static String getFileName() {
//        Random random = new Random();
//        int randomNum = random.nextInt(999);
//        String fileName = new DateTime().toString("yyyyMMddhhmmss") + randomNum;
//        return fileName;
//    }
//
//}
