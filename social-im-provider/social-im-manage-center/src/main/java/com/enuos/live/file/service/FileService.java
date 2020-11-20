package com.enuos.live.file.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.common.constants.Constants;
//import com.enuos.live.file.fastdfs.FastDFSClient;
//import com.enuos.live.file.fastdfs.FastDFSException;
import com.enuos.live.file.pojo.ImgResult;
import com.enuos.live.mapper.PicInfoMapper;
import com.enuos.live.pojo.PicInfo;
import com.enuos.live.result.Result;
import com.enuos.live.utils.AliYunOssClient;
import com.enuos.live.utils.ZimgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

//import com.enuos.live.utils.FtpUtils;

/**
 * @ClassName FileService
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/10
 * @Version V1.0
 **/
@Service
@Slf4j
public class FileService {

    private static final String URL_PIC = "http://192.168.0.50:4869/";

//    private static final String PIC_P = "?p=0";

    private String filePath = "uploadFile";

    private String httpPath = "192.168.0.114:7100/files/downloadFile/";

    private String fastDFSHttpSecretKey = "HandFastDFSToken";

    private String fileServerAddr = "http://192.168.0.50";

    @Autowired
    private PicInfoMapper picInfoMapper;

//    @Autowired
//    private FastDFSClient fastDFSClient;

    @Autowired
    private TaskAsync taskAsync;

    @Value("${aliyun.ossRate}")
    private float rate;

    @Resource(name = "taskFxbDrawExecutor")
    ExecutorService executorService;

    /**
     * @MethodName: delFile
     * @Description: TODO 删除Ali云文件
     * @Param: [objectName]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/20
     **/
    public Result delFile(String objectName) {

        AliYunOssClient.delFile(objectName);
        picInfoMapper.deleteByPrimaryKey(objectName);

        return Result.success();
    }

    public Result asyncUploadOssFile(MultipartFile multipartFile, String h, String w, String folder, HttpServletRequest request) {
        try {
            Future<Result> submit = executorService.submit(new Callable<Result>() {
                                                               @Override
                                                               public Result call() throws Exception {
                                                                   return uploadOssFile(multipartFile, h, w, folder, request);
                                                               }
                                                           }
            );
            return submit.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return Result.error();
    }

    /**
     * @MethodName: uploadOssFile
     * @Description: TODO 文件上传到阿里云服务
     * @Param: [multipartFile]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/20
     **/
    @Transactional
    public Result uploadOssFile(MultipartFile multipartFile, String h, String w, String folder, HttpServletRequest request) {
        String userId = request.getHeader("userId") == null ? "" : request.getHeader("userId");
        String fileName = multipartFile.getOriginalFilename(); // 获取原文件名
        String originalFileName = fileName.substring(fileName.lastIndexOf("/") + 1);// 原文件名
        log.info("文件上传到阿里云服务, 文件名=[{}]", originalFileName);
        String ext = getExt(originalFileName);// 文件后缀名
        String newFileName = IdUtil.simpleUUID() + ext; // 生成新文件名
        // 文件夹处理
        String filePath = filePathHandle(folder, userId, ext);
        String finalFileName = filePath + newFileName;

        String fileUrl = AliYunOssClient.uploadFile(multipartFile, finalFileName, ext);// 上传
        PicInfo record = new PicInfo();
        record.setPicName(originalFileName);
        record.setPicUrl(fileUrl);
        record.setLittlePicUrl(urlSuffix(fileUrl, h, w));
        record.setPicNewName(finalFileName);
        record.setPicType(ext);
        picInfoMapper.insert(record);
        return Result.success(record);
    }

    /**
     * @MethodName: getExt
     * @Description: TODO 获取文件后缀名
     * @Param: [fileName]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 17:01 2020/10/16
     **/
    public String getExt(String fileName) {
        String ext = null;
        if (fileName.contains(".9")) {
            ext = fileName.substring(fileName.indexOf(".9")).toLowerCase();
        } else {
            ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }
        return ext;
    }

    public static void main(String[] args) {
        String originalFileName = "loding_0.6c45b4a158.png";
        String substring = originalFileName.substring(originalFileName.lastIndexOf("/") + 1);
        System.out.println(substring);
        String ext = null;
        if (substring.contains(".9")) {
            ext = substring.substring(substring.indexOf(".9")).toLowerCase();
        } else {
            ext = substring.substring(substring.lastIndexOf(".")).toLowerCase();
        }
        System.out.println(ext);
    }

    /**
     * @MethodName: uploadOssFile
     * @Description: TODO 上传网络流到阿里云服务
     * @Param: [fileUrl:文件URL, ext:文件格式, folder:目录]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/20
     **/
    public Result uploadOssFile(String fileUrl, String ext, String folder, HttpServletRequest request) {
        String userId = request.getHeader("userId") == null ? "" : request.getHeader("userId");
        String originalFileName = fileUrl; // 获取原文件名为原URL
        log.info("文件上传到阿里云服务, 文件名=[{}]", originalFileName);
        if (StrUtil.isEmpty(ext)) {
            ext = ".jpeg";
        }
        String newFileName = IdUtil.simpleUUID() + ext; // 生成新文件名
        // 文件夹处理
        String filePath = filePathHandle(folder, userId, ext);

        String finalFileName = filePath + newFileName;

        String nweFileUrl = AliYunOssClient.uploadFile(fileUrl, finalFileName, ext);// 上传
        PicInfo record = new PicInfo();
        record.setPicName(originalFileName);
        record.setPicUrl(nweFileUrl);
        record.setPicNewName(finalFileName);
        record.setPicType(ext);
        picInfoMapper.insert(record);
        return Result.success(record);
    }

    // 文件夹处理
    private String filePathHandle(String folder, String userId, String ext) {

        String filePath = "";
        if (StrUtil.isNotEmpty(folder)) {
            if (!folder.endsWith("/")) {
                folder = folder + "/";
            }
            if (folder.startsWith("/")) {
                folder = folder.substring(1, folder.length());
            }
            if (StrUtil.isNotEmpty(userId)) {
                folder = folder + userId + "/";
            }
            filePath = AliYunOssClient.createFolder(folder);
        }
        // 如果folder为空则按照文件类型进行分类
        if (StrUtil.isEmpty(filePath)) {
            if (Constants.picExtList.contains(ext)) { // 图片
                filePath = "picture/";
            } else if (Constants.audioExtList.contains(ext)) { // 音频
                filePath = "audio/";
            } else if (Constants.videoExtList.contains(ext)) { // 视频
                filePath = "video/";
            } else {
                filePath = "file/";
            }
        }
        return filePath;
    }

    /**
     * @MethodName: gainFile
     * @Description: TODO 获取文件
     * @Param: [fileName 文件名, response, request]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/29
     **/
//    public void gainFile(String fileName, HttpServletResponse response, HttpServletRequest request) {
//        InputStream inputStream = AliYunOssClient.gainFile(fileName);
//        AliYunOssClient.writeFile(response, inputStream);
//
//        InputStream inputStream = null;
////        String urlPath = "http://7lestore.oss-cn-hangzhou.aliyuncs.com/"+fileName;
////        InputStream inputStream = AliYunOssClient.getInputStream(urlPath);
////        AliYunOssClient.writeFile(response, inputStream);
//        OutputStream out = null;
//        try {
//            inputStream = AliYunOssClient.gainFile(fileName);
//            // 通过response获取ServletOutputStream对象(out)
//            out = response.getOutputStream();
//
//            int b = 0;
//            byte[] buffer = new byte[1024];
//            while ((b = inputStream.read(buffer)) != -1) {
//                // 4.写到输出流(out)中
//                out.write(buffer, 0, b);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//                if (out != null) {
//                    out.flush();
//                    out.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }


    /**
     * @MethodName: gainFile
     * @Description: TODO 获取压缩图片文件
     * @Param: [fileName 文件名,width 缩放宽度, response, request]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/5/29
     **/
    public void gainFileImg(String fileName, String width, String high, HttpServletResponse response, HttpServletRequest request) {
        String urlPath = "http://7lestore.oss-cn-hangzhou.aliyuncs.com/" + fileName + "?x-oss-process=image/resize,w_100";
        InputStream inputStream = AliYunOssClient.getInputStream(urlPath);
        AliYunOssClient.writeFile(response, inputStream);

    }

//    public Result uploadChatFile(MultipartFile multipartFile) throws Exception {
//        // 获取原文件名
//        String fileName = multipartFile.getOriginalFilename();
//        // 文件后缀名
//        String ext = fileName.substring(fileName.lastIndexOf("."));
//        if (!Constants.picExtList.contains(ext)) {
////            Result result = nuploadFile(multipartFile, request);
//            Result result = uploadChatSample(multipartFile); // 非图片保存到FastDfs服务
//            return result;
//        }
//        // 图片保存到Zimg服务
//        ImgResult upload = ZimgUtils.upload(multipartFile, ext);
//
//        if (upload.isRet()) {
//            PicInfo record = new PicInfo();
//            String picNewName = upload.getInfo().getMd5();// 新图片名
//            record.setPicName(fileName);
//            record.setPicUrl(URL_PIC + picNewName /*+ PIC_P*/);
//            record.setPicNewName(picNewName);
//            record.setPicType(ext);
//            picInfoMapper.insert(record);
//            return Result.success(record);
//        } else {
//            return Result.error(554, upload.getError().getMessage());
//        }
//    }

//    private Result uploadChatSample(MultipartFile file) {
//        PicInfo record = new PicInfo();
//        try {
//            // 上传到服务器
//            String filepath = fastDFSClient.uploadFileWithMultipart(file);
//            record.setPicName(file.getOriginalFilename());
//            record.setPicNewName(filepath);
//            record.setPicType(FastDFSClient.getFilenameSuffix(file.getOriginalFilename()));
//            // 设置访文件的Http地址. 有时效性.l
////            String token = FastDFSClient.getToken(filepath, fastDFSHttpSecretKey);
//            record.setPicUrl(fileServerAddr + "/" + filepath /*+ "?" + token*/);
//            picInfoMapper.insert(record);
//        } catch (FastDFSException e) {
//            return Result.error(Integer.parseInt(e.getCode()), e.getMessage());
//        }
//
//        return Result.success(record);
//    }

    // =================================================================================

//    public Result uploadFile(MultipartFile multipartFile, HttpServletRequest request) throws Exception {
//
//        // 获取原文件名
//        String fileName = multipartFile.getOriginalFilename();
//        // 文件后缀名
//        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
//        if (!Constants.picExtList.contains(ext)) {
////            Result result = nuploadFile(multipartFile, request);
//            Result result = uploadSample(multipartFile, request); // 非图片保存到FastDfs服务
//            return result;
//        }
//        // 图片保存到Zimg服务
//        ImgResult upload = ZimgUtils.upload(multipartFile, ext);
//
//        if (upload.isRet()) {
//            PicInfo record = new PicInfo();
//            String picNewName = upload.getInfo().getMd5();// 新图片名
//            record.setPicName(fileName);
//            record.setPicUrl(URL_PIC + picNewName /*+ PIC_P*/);
//            record.setPicNewName(picNewName);
//            record.setPicType(ext);
//            picInfoMapper.insert(record);
//            return Result.success(record);
//        } else {
//            return Result.error(554, upload.getError().getMessage());
//        }
//    }


//    private Result uploadSample(MultipartFile file, HttpServletRequest request) {
//        PicInfo record = new PicInfo();
//        try {
//            // 上传到服务器
//            String filepath = fastDFSClient.uploadFileWithMultipart(file);
//            record.setPicName(file.getOriginalFilename());
//            record.setPicNewName(filepath);
//            record.setPicType(FastDFSClient.getFilenameSuffix(file.getOriginalFilename()));
//            // 设置访文件的Http地址. 有时效性.l
////            String token = FastDFSClient.getToken(filepath, fastDFSHttpSecretKey);
//            record.setPicUrl(fileServerAddr + "/" + filepath /*+ "?" + token*/);
//            picInfoMapper.insert(record);
//        } catch (FastDFSException e) {
//            return Result.error(Integer.parseInt(e.getCode()), e.getMessage());
//        }
//
//        return Result.success(record);
//    }

    /**
     * @MethodName: deleteFile
     * @Description: TODO 根据指定的路径删除服务器文件
     * @Param: [filePath, locale]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/4/24
     **/
//    public Result deleteFile(String filePath, Locale locale) {
//        int delFlag;
//        try {
//            delFlag = fastDFSClient.deleteFile(filePath);
//        } catch (FastDFSException e) {
//            e.printStackTrace();
//            return Result.error(Integer.parseInt(e.getCode()), e.getMessage());
//        }
//        if (delFlag == 0) {
//            // 删除数据库对应信息
//            return Result.success();
//        }
//        return Result.error(22122, "删除失败");
//    }

    private String urlSuffix(String fileUrl, String h, String w) {

        if (StrUtil.isNotEmpty(h) && StrUtil.isNotEmpty(w)) {
            return fileUrl + "?x-oss-process=image/resize,m_fixed,h_" + h + ",w_" + w;
        }
        if (StrUtil.isNotEmpty(h)) {
            return fileUrl + "?x-oss-process=image/resize,h_" + h;
        }
        if (StrUtil.isNotEmpty(w)) {
            return fileUrl + "?x-oss-process=image/resize,w_" + w;
        }
        return null;
    }


    public void scanResultsCallback(String checksum, String content) {
        try {
            if (StrUtil.isNotEmpty(content)) {
                JSONObject parse = JSON.parseObject(content);
                log.info("OSS违规检测回调结果=[{}]", parse);

                String fileName = parse.getString("object"); // OSS文件名
                boolean freezed = parse.getBoolean("freezed"); //对象是否被冻结，true表示被冻结，false表示未被冻结。
                boolean stock = parse.getBoolean("stock"); // 是否是存量对象，true表示是存量，false表示是增量。
                String region = parse.getString("region"); // OSS文件所在地域
                String bucket = parse.getString("bucket"); // OSS bucket的名称
                JSONObject scanResult = parse.getJSONObject("scanResult"); // 扫描结果
                int requestCode = scanResult.getIntValue("code");
                log.info(requestCode + "");
                JSONArray taskResults = scanResult.getJSONArray("results"); // 每一张图片的检测结果。
                log.info("每一张图片的检测结果:" + taskResults);
                if (200 == requestCode) {
                    for (Object taskResult : taskResults) {
                        String scene = ((JSONObject) taskResult).getString("scene");// porn:色黄 terrorism:暴恐涉政 ad:图文违规 qrcode:图片二维码 live:图片不良场景 logo:图片logo
                        String label = ((JSONObject) taskResult).getString("label"); // normal：正常图片 参考阿里文档
                        float rate = ((JSONObject) taskResult).getFloat("rate"); // rate：分值
                        //根据scene和label做相关处理。
                        log.info("scene = [{}]", scene);
                        log.info("label = [{}]", label);
                        log.info("rate = [{}], this.rate=[{}]", rate, this.rate);
                        if (!"normal".equals(label)) {
                            if (rate > this.rate) {
                                PicInfo picInfo = new PicInfo();
                                picInfo.setPicNewName(fileName);
                                picInfo.setIsViolation(1);
                                picInfoMapper.updateIsViolation(picInfo);// 更新图片标记为违规

                                // 异步处理
                                taskAsync.upPic(fileName);
                                break;
                            }
                        }
                    }
                } else {
                    /**
                     * 表明请求整体处理失败，原因视具体的情况详细分析。
                     */
                    log.info("the whole image scan request failed. response:" + JSON.toJSONString(parse));
                }
                //每一张图片的检测结果。
            } else {
                log.info("content参数为空");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}