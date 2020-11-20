package com.enuos.live.file;

import com.enuos.live.file.service.TaskAsync;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName UpFileController
 * @Description: TODO
 * @Author xubin
 * @Date 2020/7/8
 * @Version V2.0
 **/
@Api("文件操作")
@RestController
@Slf4j
@RequestMapping("/upFile")
public class UpFileController {

    @Autowired
    private TaskAsync taskAsync;

    @ApiOperation("删除聊天文件")
    @GetMapping("/delRoomChatFile")
    public void delRoomChatFile(@RequestParam("fileName") String fileName) {
        taskAsync.delRoomChatFile(fileName);
    }
}
