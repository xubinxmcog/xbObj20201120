package com.enuos.live.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * ftp上传下载工具类
 *
 * @author WangCaiWen Created on 2020/4/15 13:34
 */
@Slf4j
public class FtpUtil {

  /**
   * Description: 向FTP服务器上传文件
   *
   * @param host FTP服务器hostname
   * @param port FTP服务器端口
   * @param username FTP登录账号
   * @param password FTP登录密码
   * @param basePath FTP服务器基础目录
   * @param filePath FTP服务器文件存放路径。例如分日期存放：/2015/01/01。文件的路径为basePath+filePath
   * @param filename 上传到FTP服务器上的文件名
   * @param input 输入流
   * @return 成功返回true，否则返回false
   */
  public static boolean uploadFile(String host, int port, String username, String password,
      String basePath
      , String filePath, String filename, InputStream input) {
    //1、创建临时路径
    String tempPath = "";
    //2、创建FTPClient对象（对于连接ftp服务器，以及上传和上传都必须要用到一个对象）
    FTPClient ftp = new FTPClient();
    try {
      //3、定义返回的状态码
      int reply;
      //4、连接ftp(当前项目所部署的服务器和ftp服务器之间可以相互通讯，表示连接成功)
      ftp.connect(host, port);
      //5、输入账号和密码进行登录
      ftp.login(username, password);
      //6、接受状态码(如果成功，返回230，如果失败返回503)
      reply = ftp.getReplyCode();
      //7、根据状态码检测ftp的连接，调用isPositiveCompletion(reply)-->如果连接成功返回true，否则返回false
      if (!FTPReply.isPositiveCompletion(reply)) {
        //说明连接失败，需要断开连接
        ftp.disconnect();
        return false;
      }
      //8、changWorkingDirectory(linux上的文件夹)：检测所传入的目录是否存在，如果存在返回true，否则返回false
      //basePath+filePath-->home/ftp/www/2019/09/02
      if (!ftp.changeWorkingDirectory(basePath + filePath)) {
        //9、截取filePath:2019/09/02-->String[]:2019,09,02
        String[] dirs = filePath.split("/");
        //10、把basePath(/home/ftp/www)-->tempPath
        tempPath = basePath;
        for (String dir : dirs) {
          //11、循环数组(第一次循环-->2019)
          //跳出本地循环，进入下一次循环
          if (null == dir || "".equals(dir)) {
            continue;
          }
          //12、更换临时路径：/home/ftp/www/2019
          tempPath += "/" + dir;
          //13、再次检测路径是否存在(/home/ftp/www/2019)-->返回false，说明路径不存在
          if (!ftp.changeWorkingDirectory(tempPath)) {
            //14、makeDirectory():创建目录  返回Boolean雷类型，成功返回true
            if (!ftp.makeDirectory(tempPath)) {
              return false;
            } else {
              //15、严谨判断（重新检测路径是否真的存在(检测是否创建成功)）
              ftp.changeWorkingDirectory(tempPath);
            }
          }
        }
      }
      //16.把文件转换为二进制字符流的形式进行上传
      ftp.setFileType(FTP.BINARY_FILE_TYPE);
      //17、这才是真正上传方法storeFile(filename,input),返回Boolean雷类型，上传成功返回true
      if (!ftp.storeFile(filename, input)) {
        return false;
      }
      // 18.关闭输入流
      input.close();
      // 19.退出ftp
      ftp.logout();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (ftp.isConnected()) {
        try {
          // 20.断开ftp的连接
          ftp.disconnect();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
    return true;
  }

  /**
   * Description: 从FTP服务器下载文件
   *
   * @param host FTP服务器hostname
   * @param port FTP服务器端口
   * @param username FTP登录账号
   * @param password FTP登录密码
   * @param basePath FTP服务器基础目录
   * @param fileName 要下载的文件名
   * @param localPath 下载后保存到本地的路径
   * @return 成功返回true，否则返回false
   */
  public static boolean downloadFile(String host, int port, String username, String password,
      String basePath
      , String fileName, String localPath) {
    boolean result = false;
    FTPClient ftp = new FTPClient();
    try {
      int reply;
      ftp.connect(host, port);
      // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
      // 登录
      ftp.login(username, password);
      reply = ftp.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        ftp.disconnect();
        return result;
      }
      // 转移到FTP服务器目录
      ftp.changeWorkingDirectory(basePath);
      FTPFile[] fs = ftp.listFiles();
      for (FTPFile ff : fs) {
        if (ff.getName()
            .equals(fileName)) {
          File localFile = new File(localPath + "/" + ff.getName());
          OutputStream is = new FileOutputStream(localFile);
          ftp.retrieveFile(ff.getName(), is);
          is.close();
        }
      }
      ftp.logout();
      result = true;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (ftp.isConnected()) {
        try {
          ftp.disconnect();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
    return result;
  }

  /**
   * Description: 从FTP服务器下载文件
   *
   * @param host FTP服务器hostname
   * @param port FTP服务器端口
   * @param username FTP登录账号
   * @param password FTP登录密码
   * @param basePath FTP服务器基础目录
   * @param fileName 要删除的文件名
   * @return 成功返回true，否则返回false
   */
  public static boolean deleteFile(String host, int port, String username, String password,
      String basePath, String fileName) {
    boolean result = false;
    FTPClient ftp = new FTPClient();
    try {
      int reply;
      // 连接FTP服务器
      ftp.connect(host, port);
      // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
      // 登录
      ftp.login(username, password);
      reply = ftp.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        ftp.disconnect();
        return result;
      }
      //切换到文件目录
      if (ftp.changeWorkingDirectory(basePath)) {
        //测试删除文件
        if (ftp.deleteFile(fileName)) {
          ftp.logout();
          result = true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (ftp.isConnected()) {
        try {
          ftp.disconnect();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
    return result;
  }

//    public static void main(String[] args) {
//        try {
//            FileInputStream in=new FileInputStream(new File("D:\\2.jpg"));
//            boolean flag = uploadFile("192.168.0.50", 21, "ftptest", "ftptest@1234", "/home/ftptest/chat/images","/2015/01/21", "asdasd11asdaa.jpg", in);
//            System.out.println(flag);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }


}
