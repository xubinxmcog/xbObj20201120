package com.enuos.live.utils.method;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * @Description 抽奖算法[离散算法]
 * @Author wangyingjie
 * @Date 2020/10/14
 * @Modified
 */
@Slf4j
public class DiscreteMethod {

    private final Random random;

    private final int[] weight;

    public DiscreteMethod(List<Integer> weightList) {
        this(weightList, new Random());
    }

    public DiscreteMethod(List<Integer> weightList, Random random) {
        if (weightList == null || random == null) {
            throw new NullPointerException();
        }

        int size = weightList.size();

        if (size == 0) {
            throw new IllegalArgumentException("DiscreteMethod weightList must be nonempty.");
        }

        this.random = random;
        weight = new int[size];

        int t = 0;
        for (int i = 0; i < size; i++) {
            weight[i] = t + weightList.get(i);
            t = weight[i];
        }
    }

    /** 
     * @Description: 取值
     * @Param: [] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/10/14 
     */ 
    public int next() {
        int num = random.nextInt(weight[weight.length-1]);

        int index = -1;
        for (int i = 0; i < weight.length; i++) {
            if (weight[i] > num) {
                index = i;
                break;
            }
        }

        return index;
    }
}
