package com.enuos.live.function;

import java.math.BigDecimal;

/**
 * @Description @FunctionalInterface 函数式接口只允许一个抽象方法[SAM接口]
 * @Author wangyingjie
 * @Date 2020/8/5
 * @Modified
 */
@FunctionalInterface
public interface ToBigDecimalFunction<T> {

    BigDecimal applyAsBigDecimal(T value);

}
