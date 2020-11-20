package com.enuos.live.controller;

import cn.hutool.core.map.MapUtil;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.annotations.NoRepeatSubmit;
import com.enuos.live.annotations.OperateLog;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.result.Result;
import com.enuos.live.service.ProductBackpackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ProductBackpackController
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/9
 * @Version V1.0
 **/
@Api("用户背包")
@RestController
@RequestMapping("/productBackpack")
@Slf4j
public class ProductBackpackController {

    @Autowired
    private ProductBackpackService productBackpackService;

    @NoRepeatSubmit(lockTime = 1)
    @ApiOperation(value = "查看我的背包", notes = "userId和分页字段必填，productId不填的话查询背包列表，填写则查询对应商品详情")
    @Cipher
    @PostMapping("/queryBackpack")
    public Result queryBackpack(@RequestBody Map<String, String> params) {
        Long userId = MapUtil.getLong(params, "userId");
        Long productId = MapUtil.getLong(params, "productId");
        Integer pageNum = MapUtil.getInt(params, "pageNum");
        Integer pageSize = MapUtil.getInt(params, "pageSize");
        Integer categoryId = MapUtil.getInt(params, "categoryId");

        if (null != userId && userId != 0) {
//            return productBackpackService.queryBackpack(userId, productId, categoryId, pageNum, pageSize);
            return productBackpackService.getUserOrnaments(userId);
        }
        return Result.error(ErrorCode.DATA_ERROR);
    }

    @OperateLog(operateMsg = "使用背包物品")
    @ApiOperation(value = "使用背包物品", notes = "userId，productId字段必填")
    @Cipher
    @PostMapping("/consumption")
    public Result use(@RequestBody Map<String, String> params) {
        log.info("使用背包物品入参=[{}]", params);
        Integer id = MapUtil.getInt(params, "id");
        Long userId = MapUtil.getLong(params, "userId");
        Integer amount = MapUtil.getInt(params, "amount");
        if (id == 0 || userId == 0) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        return productBackpackService.use(id, userId, amount);
    }

    @OperateLog(operateMsg = "游戏物品加载查询")
    @ApiOperation(value = "游戏物品加载查询", notes = "userId，productId字段必填")
    @GetMapping("/gameDecorate")
    public List gameDecorate(@RequestParam("userId") Long userId, @RequestParam("gameCode") Integer gameCode) {
        log.info("游戏物品使用入参: userId=[{}], gameCode=[{}]", userId, gameCode);
        if (gameCode == 0 || userId == 0) {
            return new ArrayList();
        }
        return productBackpackService.gameDecorate(userId, gameCode);
    }

    /**
     * 批量添加背包（后端服务调用）
     *
     * @param params
     * @return
     */
    @PostMapping("/addBackpack")
    public Result addBackpack(@RequestBody Map<String, Object> params) {
        log.info("批量添加背包参数=[{}]", params);
        return productBackpackService.addBackpack(params);

    }

}
