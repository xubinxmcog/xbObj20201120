package com.enuos.live.utils.method;

import com.enuos.live.utils.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Description 抽奖算法[别名算法]
 * @Author wangyingjie
 * @Date 2020/9/16
 * @Modified
 */
@Slf4j
public class AliasMethod {

    private final Random random;

    // 补色下标
    private final int[] alias;

    // 原色*大小
    private final double[] probability;

    /**
     * @Description: 构造函数
     * @Param: [probabilities] 原色概率
     * 我们用原色标识奖品，原色+补色=1，每组中最多只有一个补色
     * @Return:
     * @Author: wangyingjie
     * @Date: 2020/9/16
     */
    public AliasMethod(List<Double> probabilities) {
        this(probabilities, new Random());
    }

    /** 
     * @Description: 构造函数
     * @Param: [probabilityList, random] 
     * @Return:  
     * @Author: wangyingjie
     * @Date: 2020/9/16 
     */ 
    public AliasMethod(List<Double> probabilityList, Random random) {
        if (probabilityList == null || random == null) {
            throw new NullPointerException();
        }

        int size = probabilityList.size();

        if (size == 0) {
            throw new IllegalArgumentException("Probability vector must be nonempty.");
        }

        // 定义两个数组，probability用于存储原色的概率, alias用于存储补色下标
        probability = new double[size];
        alias = new int[size];
        this.random = random;

        final double average = BigDecimalUtil.nDiv(1.0, (double)size);

        probabilityList = new ArrayList<Double>(probabilityList);

        // 模拟队列用于存储大小概率
        Deque<Integer> small = new ArrayDeque<Integer>();
        Deque<Integer> large = new ArrayDeque<Integer>();

        for (int i = 0; i < size; ++i) {
            if (probabilityList.get(i) >= average) {
                large.add(i);
            } else {
                small.add(i);
            }
        }

        while (!small.isEmpty() && !large.isEmpty()) {
            // 取出大小队列中的最后下标，less原色，more为补色
            int less = small.removeLast();
            int more = large.removeLast();

            probability[less] = BigDecimalUtil.nMul(probabilityList.get(less), (double)size);
            alias[less] = more;

            probabilityList.set(more, BigDecimalUtil.nSub(BigDecimalUtil.nAdd(probabilityList.get(more), probabilityList.get(less)), average));

            // 当补色小于平均，则补色转原色
            if (probabilityList.get(more) >= average) {
                large.add(more);
            } else {
                small.add(more);
            }
        }

        while (!small.isEmpty()) {
            probability[small.removeLast()] = 1.0;
        }

        while (!large.isEmpty()) {
            probability[large.removeLast()] = 1.0;
        }
    }

    /**
     * @Description: 抽奖
     * @Param: []
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/16
     */
    public int next() {
        int column = random.nextInt(probability.length);

        boolean coinToss = random.nextDouble() < probability[column];

        return coinToss ? column : alias[column];
    }

}
