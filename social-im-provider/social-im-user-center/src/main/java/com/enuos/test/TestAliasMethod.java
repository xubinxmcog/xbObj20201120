package com.enuos.test;

import com.enuos.live.cipher.AESEncoder;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/16
 * @Modified
 */
public class TestAliasMethod {

    public static void main(String[] args) {
        System.out.println(AESEncoder.decrypt(1600257194L,"UCM+IU9rqcr7jZpd9NjUFSNG4sHK9tFfdn/+BwFkw3k\\u003d"));
    }
}
