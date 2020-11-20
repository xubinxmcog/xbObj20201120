package com.enuos.live.feign;

import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/8/10
 * @Modified
 */
@Component
@FeignClient(name = "SOCIAL-IM-MANAGE")
public interface ManageFeign {
    
    /** 
     * @Description: 上传文件
     * @Param: [fileUrl, folder] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/8/10 
     */ 
    @GetMapping(value = "/files/uploadFileUrl")
    Result uploadFileUrl(@RequestParam("fileUrl") String fileUrl, @RequestParam("folder") String folder);
}
