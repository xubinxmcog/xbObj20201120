package com.enuos.live.file;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.file.service.FileService;
import com.enuos.live.mapper.PicInfoMapper;
import com.enuos.live.result.Result;
import com.enuos.live.utils.AliYunOssClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * @ClassName FileUploadController
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/10
 * @Version V1.0
 **/
@Api("文件上传下载")
@RestController
@Slf4j
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private PicInfoMapper picInfoMapper;

    // 上传文件路径配置 区分生产和开发环境
    @Value("${uploadFile.path}")
    private String uploadFilePath;

    // 文件扫描回调seed
    @Value("${uploadFile.scanSeed}")
    private String scanSeed;

    @CrossOrigin
    @ApiOperation("后台使用上传文件")
    @PostMapping("/secret/uploadDocument")
    public Result uploadDocument(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "h", required = false) String h,
                                 @RequestParam(value = "w", required = false) String w,
                                 @RequestParam(value = "folder", required = false) String folder,
                                 HttpServletRequest request) throws Exception {
        if ("pro".equals(uploadFilePath)) { // 生产环境
            return fileService.uploadOssFile(file, h, w, folder, request);
        } else { // 开发环境
            return fileService.uploadOssFile(file, h, w, folderHandle(folder), request);
        }
    }

    @CrossOrigin
    @ApiOperation("上传文件")
    @PostMapping("/uploadFile")
    public Result uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "h", required = false) String h,
                             @RequestParam(value = "w", required = false) String w,
                             @RequestParam(value = "folder", required = false) String folder,
                             HttpServletRequest request) throws Exception {
        if ("pro".equals(uploadFilePath)) { // 生产环境
            return fileService.asyncUploadOssFile(file, h, w, folder, request);
        } else { // 开发环境
            return fileService.asyncUploadOssFile(file, h, w, folderHandle(folder), request);
//            return fileService.uploadFile(file, request);
        }
    }

    /**
     * @MethodName: uploadOssFile
     * @Description: TODO 上传网络流到阿里云服务
     * @Param: [fileUrl, folder, ext, request]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:49 2020/8/10
     **/
    @GetMapping("/uploadFileUrl")
    public Result uploadOssFile(@RequestParam("fileUrl") String fileUrl, @RequestParam("folder") String folder,
                                String ext, HttpServletRequest request) {
        if ("pro".equals(uploadFilePath)) { // 生产环境
            return fileService.uploadOssFile(fileUrl, ext, folder, request);
        } else { // 开发环境
            return fileService.uploadOssFile(fileUrl, ext, folderHandle(folder), request);
        }
    }

    /**
     * @MethodName: delFile
     * @Description: TODO 删除阿里云文件
     * @Param: [objectName]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/20
     **/
    @PostMapping("/delFileObjectName")
    public Result delFile(String objectName) {
        return fileService.delFile(objectName);
    }

    @RequestMapping("/test")
    public Result test(String bucketName) {
//        Boolean Boolean = AliYunOssClient.doesBucketExist(bucketName);

        Map<String, Object> map = picInfoMapper.selectPicUrl(bucketName);// 根据文件名查找URL
        String picUrl = map.get("picUrl").toString();

        Map<String, Object> stringStringMap = picInfoMapper.queryPostResource(picUrl); // 根据URL查找用户动态资源表的相关文件URL

        String id = String.valueOf(stringStringMap.get("id"));
        String url = (String) stringStringMap.get("url") + "1";
        String coverUrl = (String) stringStringMap.get("coverUrl") + "1";
        String thumbUrl = (String) stringStringMap.get("thumbUrl") + "1";
        log.info(id);
        log.info(url);
        log.info(coverUrl);
        log.info(thumbUrl);
//        picInfoMapper.updatePostResourceUrl(url, coverUrl, thumbUrl, id);
        return Result.success(stringStringMap);
    }

    @ApiOperation("文件扫描结果回调")
    @RequestMapping(value = "/scan/results/callback")
    public void scanResultsCallback(String checksum, String content) {
        //checksum = c4be8ee7882f9b728046e8f7fd4088a39ba6d5097a78d9ee4c6b87eaea1ebaa2
        log.info("文件扫描结果回调,checksum=[{}], content=[{}]", checksum, content);

        fileService.scanResultsCallback(checksum, content);

    }

    private String folderHandle(String folder) {
        if (StrUtil.isEmpty(folder)) {
            folder = "dev/";
        } else {
            if (folder.startsWith("/")) {
                folder = "dev" + folder;
            } else {
                folder = "dev/" + folder;
            }
        }
        return folder;
    }

//    @ApiOperation("下载文件")
//    @GetMapping("/gainFile/{fileName}")
//    public void gainFile(@PathVariable("fileName") String fileName, HttpServletResponse response, HttpServletRequest request) {
//
//        fileService.gainFile(fileName, response, request);
//    }

    /**
     * 根据指定的路径删除服务器文件，适用于没有保存数据库记录的文件
     *
     * @param// filePath
     */
//    @RequestMapping("/deleteFile")
//    public Result deleteFile(String filePath, Locale locale) {
//        return fileService.deleteFile(filePath, locale);
//    }

}
