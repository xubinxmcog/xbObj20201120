package com.enuos.live.file.pojo;

import lombok.Data;

/**
 * @ClassName ImgResult
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/22
 * @Version V1.0
 **/
@Data
public class ImgResult {

    private boolean ret;
    private ImgError error;
    private ImgInfo info;

}
