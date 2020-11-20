package com.enuos.test;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/12
 * @Modified
 */
public class TestLamda {

    static List<Apple> filterList(List<Apple> list, AppleFactory appleFactory) {
        return list.stream().filter(s -> appleFactory.compare(s)).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Apple> list = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0) {
                list.add(new Apple("red", Double.valueOf(i + 10)));
            } else {
                list.add(new Apple("green", Double.valueOf(i + 10)));
            }
        }

        System.out.println(list);


        list = filterList(list, l->l.getColor().equals("red"));

        System.out.println(list);
    }

}

interface AppleFactory {
    boolean compare(Apple apple);
}

@Data
@AllArgsConstructor
class Apple {

    private String color;

    private Double weigth;

}