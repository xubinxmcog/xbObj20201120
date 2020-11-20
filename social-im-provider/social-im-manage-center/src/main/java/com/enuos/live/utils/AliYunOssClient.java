package com.enuos.live.utils;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.*;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

/**
 * @ClassName AliYunOssClient
 * @Description: TODO 阿里云文件上传
 * @Author xubin
 * @Date 2020/5/20
 * @Version V1.0
 **/
@Slf4j
public class AliYunOssClient {

    private static Map<String, String> extensionMap = new HashMap<>();

    private final static String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private final static String accessKeyId = "LTAI4GGmEkNnbz5Nq3B8kbNJ";
    private final static String accessKeySecret = "niARnN93xkH3Cjp77DfHiPj8MicuVF";
    private final static String bucketName = "7lestore";
    private final static String fileUrl = "https://7lestore.oss-cn-hangzhou.aliyuncs.com";

    /**
     * @MethodName: getOSSClient
     * @Description: TODO 获取阿里云OSS客户端对象
     * @Param: []
     * @Return: com.aliyun.oss.OSS
     * @Author: xubin
     * @Date: 2020/6/2
     **/
    public static OSS getOSSClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * @MethodName: uploadFile
     * @Description: TODO 文件上传
     * @Param: [file 文件, fileName 文件名, filePath 路径]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 2020/5/29
     **/
    public static String uploadFile(MultipartFile file, String fileName, String ext) {

        long startTime = System.currentTimeMillis();
        // 构造客户端实例
        OSS ossClient = getOSSClient();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            String getcontentType = getcontentType(ext);
            if (StrUtil.isNotEmpty(getcontentType)) {
                metadata.setContentType(getcontentType);
            }
//            metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());

//            ossClient.setBucketAcl(bucketName,CannedAccessControlList.PublicRead); // 存储空间（Bucket） 公共读，私有写
            metadata.setObjectAcl(CannedAccessControlList.PublicRead); // 存储对象（Object） 公共读，私有写

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, new ByteArrayInputStream(file.getBytes()));
//            PutObjectResult putObjectResult = ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(file.getBytes()), metadata);
//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, new ByteArrayInputStream(file.getBytes()));
//            Date expiration = new Date(new Date().getTime() + 3600l * 1000 * 24 * 365 * 1);// 1年

            putObjectRequest.setMetadata(metadata);
            // 上传
            ossClient.putObject(putObjectRequest);
            long uploadTime = System.currentTimeMillis() - startTime;
            log.info("本次上传花费时间[{}]毫秒", uploadTime);
            return fileUrl + "/" + fileName;
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message: " + oe.getErrorMessage());
            log.error("Error Code:       " + oe.getErrorCode());
            log.error("Request ID:      " + oe.getRequestId());
            log.error("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ce.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭客户端以释放所有分配的资源。
            ossClient.shutdown();
        }
        return null;
    }

    /**
     * @MethodName: uploadFile
     * @Description: TODO 上传网络流
     * @Param: [file 文件, fileName 文件名, filePath 路径]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 2020年8月4日 09:04:34
     **/
    public static String uploadFile(String fileUrl, String fileName, String ext) {
        // 构造客户端实例
        OSS ossClient = getOSSClient();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            String getcontentType = getcontentType(ext);
            if (StrUtil.isNotEmpty(getcontentType)) {
                metadata.setContentType(getcontentType);
            }
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, new URL(fileUrl).openStream());
            putObjectRequest.setMetadata(metadata);

            // 上传网络流。
            ossClient.putObject(putObjectRequest);

            return AliYunOssClient.fileUrl + "/" + fileName;
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message: " + oe.getErrorMessage());
            log.error("Error Code:       " + oe.getErrorCode());
            log.error("Request ID:      " + oe.getRequestId());
            log.error("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ce.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭客户端以释放所有分配的资源。
            ossClient.shutdown();
        }
        return null;

    }

    /**
     * @MethodName: gainFile
     * @Description: TODO 从阿里云获取文件
     * @Param: []
     * @Return: java.io.BufferedReader
     * @Author: xubin
     * @Date: 2020/5/29
     **/
    public static InputStream gainFile(String fileName) {
        // 创建OSSClient实例
//        OSS ossClient = getOSSClient();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
        OSSObject ossObject = ossClient.getObject(bucketName, fileName);
        InputStream content = ossObject.getObjectContent();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));

//        // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
//        try {
//            content.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // 关闭OSSClient。
        ossClient.shutdown();

        return content;

    }

    public static void gainFileImg(String fileName, String width) {
        // 创建OSSClient实例。
        OSS ossClient = getOSSClient();
        if (StrUtil.isNotEmpty(width)) {
            // 图片缩放。
            String style = "image/resize,m_fixed,w_" + width;
            GetObjectRequest request = new GetObjectRequest(bucketName, "");
            request.setProcess(style);
            ossClient.getObject(request, new File(fileName));
        }
    }

    /**
     * @MethodName: createFolder
     * @Description: TODO 创建模拟文件夹
     * @Param: [folder]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 2020/6/2
     **/
    public static String createFolder(String folder) {
        //文件夹名
        final String keySuffixWithSlash = folder;
        try {
            OSS ossClient = getOSSClient();

            // 判断文件夹是否存在, 不存在则创建
            if (!ossClient.doesObjectExist(bucketName, keySuffixWithSlash)) {
                //创建文件夹
                ossClient.putObject(bucketName, keySuffixWithSlash, new ByteArrayInputStream(new byte[0]));
                log.info("创建文件夹成功");
                //得到文件夹名
                OSSObject object = ossClient.getObject(bucketName, keySuffixWithSlash);
                String fileDir = object.getKey();
                return fileDir;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return keySuffixWithSlash;

    }

    /**
     * @MethodName: doesBucketExist
     * @Description: TODO 判断存储空间是否存在
     * @Param: [bucketName]
     * @Return: java.lang.Boolean
     * @Author: xubin
     * @Date: 2020/6/2
     **/
    public static Boolean doesBucketExist(String bucketName) {
        // 创建OSSClient实例。
        OSS ossClient = getOSSClient();

        boolean exists = false;
        try {
            exists = ossClient.doesBucketExist(bucketName);
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message: " + oe.getErrorMessage());
            log.error("Error Code:       " + oe.getErrorCode());
            log.error("Request ID:      " + oe.getRequestId());
            log.error("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ce.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return exists;
    }


    /**
     * @MethodName: delFile
     * @Description: TODO 删除阿里云图片
     * @Param: [objectName]
     * @Return: void
     * @Author: xubin
     * @Date: 13:41 2020/7/8
     **/
    public static void delFile(String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = getOSSClient();
        try {
            // 删除文件。如需删除文件夹，请将ObjectName设置为对应的文件夹名称。如果文件夹非空，则需要将文件夹下的所有object删除后才能删除该文件夹。
            ossClient.deleteObject(bucketName, objectName);
        } catch (ClientException ce) {
            System.out.println("删除异常: 文件名=" + objectName);
        } finally {

            // 关闭OSSClient。
            ossClient.shutdown();
        }

    }

    public static void delPrefixFile(String prefix) {
        // 创建OSSClient 实例
        OSS ossClient = getOSSClient();

        // 列举所有包含指定前缀的文件并删除
        String nexMarker = null;
        ObjectListing objectListing = null;

        do {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName)
                    .withPrefix(prefix)
                    .withMarker(nexMarker);
            objectListing = ossClient.listObjects(listObjectsRequest);
            if (objectListing.getObjectSummaries().size() > 0) {
                List<String> keys = new ArrayList<>();
                for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    log.info("key name: " + objectSummary.getKey());
                    keys.add(objectSummary.getKey());
                }
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
                        .withKeys(keys);
                ossClient.deleteObjects(deleteObjectsRequest);
            }
            nexMarker = objectListing.getNextMarker();
            System.out.println(nexMarker);
        } while (objectListing.isTruncated());
        ossClient.shutdown();
    }

    public static String getcontentType(String filenameExtension) {
//        String toLowerCase = filenameExtension.toLowerCase();
        String extension = extensionMap.get(filenameExtension);
        if (StrUtil.isEmpty(extension)) {
            return null;
        }
        return extension;

    }

    static {
        extensionMap.put(".bmp", "image/bmp");
        extensionMap.put(".gif", "image/gif");
        extensionMap.put(".jpeg", "image/jpg");
        extensionMap.put(".jpg", "image/jpg");
        extensionMap.put(".png", "image/jpg");
        extensionMap.put(".html", "text/html");
        extensionMap.put(".txt", "text/plain");
        extensionMap.put(".vsd", "application/vnd.visio");
        extensionMap.put(".pptx", "application/vnd.ms-powerpoint");
        extensionMap.put(".ppt", "application/vnd.ms-powerpoint");
        extensionMap.put(".docx", "application/msword");
        extensionMap.put(".doc", "application/msword");
        extensionMap.put(".xml", "text/xml");
    }

    /**
     * @MethodName: getInputStream
     * @Description: TODO 从服务器获得一个输入流(本例是指从服务器获得一个image输入流)
     * @Param: [urlPath]
     * @Return: java.io.InputStream
     * @Author: xubin
     * @Date: 2020/6/2
     **/
    public static InputStream getInputStream(String urlPath) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlPath);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // 设置网络连接超时时间
            httpURLConnection.setConnectTimeout(3000);
            // 设置应用程序要从网络连接读取数据
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            log.info("responseCode is:" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 从服务器返回一个输入流
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    /**
     * @MethodName: writeFile
     * @Description: TODO 将输入流输出到页面
     * @Param: [resp, inputStream]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/6/2
     **/
    public static void writeFile(HttpServletResponse resp, InputStream inputStream) {
        OutputStream out = null;
        try {
            out = resp.getOutputStream();
            int len = 0;
            byte[] b = new byte[1024];
            while ((len = inputStream.read(b)) != -1) {
                out.write(b, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
