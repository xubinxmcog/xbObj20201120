package com.enuos.live.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;

/**
 * @ClassName CreateFolderSample
 * @Description: TODO
 * @Author xubin
 * @Date 2020/5/20
 * @Version V1.0
 **/
public class CreateFolderSample {

    private static String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private static String accessKeyId = "LTAI4GGmEkNnbz5Nq3B8kbNJ";
    private static String accessKeySecret = "niARnN93xkH3Cjp77DfHiPj8MicuVF";
    private static String bucketName = "7lestore";

    public static void main(String[] args) throws IOException {
        /*
         * 用你的账号构造一个客户端实例访问OSS
         */
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            /*
             * 创建一个没有请求正文的空文件夹，注意密钥必须以斜线作为后缀
             */
            final String keySuffixWithSlash = "productPic/";
            client.putObject(bucketName, keySuffixWithSlash, new ByteArrayInputStream(new byte[0]));
            System.out.println("创建空文件夹 " + keySuffixWithSlash + "\n");

            /*
             * 验证空文件夹的大小是否为空
             */
            OSSObject object = client.getObject(bucketName, keySuffixWithSlash);
            System.out.println("文件夹大小 '" + object.getKey() + "' 是 " +
                    object.getObjectMetadata().getContentLength());
            object.getObjectContent().close();

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorMessage());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
        } finally {
            /*
             * 关闭客户端以释放所有分配的资源。
             */
            client.shutdown();
        }
    }
}
