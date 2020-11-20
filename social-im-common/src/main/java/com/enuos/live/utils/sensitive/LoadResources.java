package com.enuos.live.utils.sensitive;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author WangCaiWen
 * Created on 2020/3/2 13:46
 */
public class LoadResources {

    /**
     * 加载类路径下资源
     *
     * @return File
     * @throws FileNotFoundException FileNotFoundException
     */
    public static File getFilterResource() throws FileNotFoundException {
        File file;
        try {
            file = ResourceUtils.getFile("classpath:filter.txt");
        } catch (FileNotFoundException e) {
            System.out.println("资源不存在");
            throw e;
        }
        return file;
    }

}
