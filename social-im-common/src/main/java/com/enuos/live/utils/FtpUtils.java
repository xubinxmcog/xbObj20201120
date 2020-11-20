//package com.enuos.live.utils;
//
//import cn.hutool.core.date.DateTime;
//import lombok.Value;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPConnectionClosedException;
//import org.apache.commons.net.ftp.FTPReply;
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
//@Component
//public class FtpUtils {
//
//    private String host = "192.168.0.50";
//
//    private int port = 21;
//
//    private String userName = "ftptest";
//
//    private String password = "ftptest@1234";
//
//    private String basePath = "/home/ftptest";
//
//    private String httpPath = "http://192.168.0.50";
//
//    private String filePath = "/uploadFile";
//
//    public static final String DIRSPLIT = "/";
//
//    // ftp客户端
//    private FTPClient ftpClient = new FTPClient();
//
//    /**
//     * @MethodName: uploadFile
//     * @Description: TODO 上传文件
//     * @Param: [
//     * fileName（文件名）,
//     * filePath（文件目录，可选，参数为空文件保存到根目录，参数不为空：如果没有该目录则创建，有则保存对应目录，如2020/04/17 or 2020）,
//     * inputStream]
//     * @Return: java.lang.Boolean
//     * @Author: xubin
//     * @Date: 2020/4/10
//     **/
//    public Boolean uploadFile(String fileName, String filePath, InputStream inputStream) throws Exception {
//
//        try {
//            connectToServer();
//            //6、接受状态码(如果成功，返回230，如果失败返回503)
//            int reply = ftpClient.getReplyCode();
//            log.info("ftp连接状态码：" + reply);
//            //7、根据状态码检测ftp的连接，调用isPositiveCompletion(reply)-->如果连接成功返回true，否则返回false
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                //说明连接失败，需要断开连接
//                ftpClient.disconnect();
//                log.info("ftp连接失败");
//                return false;
//            }
//            log.info("ftp连接成功！");
//            if (null == filePath || "".equals(filePath)) {
//                // 修改操作空间
//                ftpClient.changeWorkingDirectory(basePath);
//            } else {
//
//                // changWorkingDirectory(linux上的文件夹)：检测所传入的目录是否存在，如果存在返回true，否则返回false
//                boolean bo = createFilePath(filePath);
//                if (!bo)
//                    return false;
//            }
//
//            // 把文件转换为二进制字符流的形式进行上传
//            setFileType(FTP.BINARY_FILE_TYPE);
//            // 上传方法storeFile(filename,input),返回Boolean类型，上传成功返回true
//            if (!ftpClient.storeFile(fileName, inputStream)) {
//                return false;
//            }
//            // 关闭输入流
//            inputStream.close();
//            // 退出ftp
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
//    public InputStream downloadFile(String fileName, String filePath) throws FTPConnectionClosedException {
//        log.info("开始FTP下载文件.........");
//
////        FTPClient ftpClient = new FTPClient();
//        InputStream in = null;
//        try {
//            long startTime = System.currentTimeMillis();
//            // 建立连接
////            connectToServer();
//            ftpClient.connect(host, port);
//            ftpClient.login(userName, password);
//
//            int reply = ftpClient.getReplyCode();
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                ftpClient.disconnect();
//                throw new IOException("failed to connect to the FTP Server:" + host);
//            }
//            // 设置传输二进制文件
////            setFileType(FTP.BINARY_FILE_TYPE);
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            // 将当前数据连接模式设置为<code>被动本地数据连接模式</code>
//            ftpClient.enterLocalPassiveMode();
//            if (!(null == filePath || "".equals(filePath)) || filePath.startsWith("/")) {
//                filePath = "/" + filePath;
//            }
//            // 更改FTP会话的当前工作目录
//            ftpClient.changeWorkingDirectory(basePath + filePath);
////            ftpClient.setRemoteVerificationEnabled(false);
//            // ftp文件获取文件
//            in = ftpClient.retrieveFileStream(fileName);
//            long entTime = System.currentTimeMillis();
//            log.info("FTP下载总耗时=【{}】",entTime-startTime);
//        } catch (FTPConnectionClosedException e) {
//            log.error("ftp连接被关闭！", e);
//            throw e;
//        } catch (Exception e) {
//            log.error("ERR : upload file " + fileName + " from ftp : failed!", e);
//        } finally {
//            if (ftpClient.isConnected()) {
//                try {
//                    ftpClient.logout();
//                    ftpClient.disconnect();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
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
//    public boolean createFilePath(String filePath) throws IOException {
//        if (!ftpClient.changeWorkingDirectory(basePath + filePath)) {
//            // 创建临时路径
//            String tempPath = "";
//            // 截取filePath
//            String[] dirs = filePath.split("/");
//            // 把basePath(/home/ftp/www)-->tempPath
//            tempPath = basePath;
//            for (String dir : dirs) {
//                //11、循环数组
//                if (null == dir || "".equals(dir))
//                    continue;
//                //12、更换临时路径：/home/ftp/www/2019
//                tempPath += "/" + dir;
//                //13、再次检测路径是否存在(/home/ftp/www/2019)-->返回false，说明路径不存在
//                if (!ftpClient.changeWorkingDirectory(tempPath)) {
//                    //14、makeDirectory():创建目录  返回Boolean类型，成功返回true
//                    if (!ftpClient.makeDirectory(tempPath)) {
//                        return false;
//                    } else {
//                        //15、严谨判断（重新检测路径是否真的存在(检测是否创建成功)）
//                        ftpClient.changeWorkingDirectory(tempPath);
//                    }
//                }
//            }
//        }
//        return true;
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
