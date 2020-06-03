package com.rzyou.funtime.game.ddz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DdzStaticData {
    //创建Map集合，键：编号     值：牌
    static Map<Integer,String> pooker = new HashMap<>();

    //牌值对
    static Map<Integer,Integer> pookerVal = new HashMap<>();

    //创建List集合，存储编号
    static List<Integer> pookerNumber = new ArrayList<>();
    //定义13个点数的数组
    static String[] numbers = {"3","4","5","6","7","8","9","10","J","Q","K","A","2"};
    //定义4个花色组合
    static String[] colors = {"R","S","T","M"};//R-黑桃S-梅花T-红桃

    static {
        //定义一个整数变量，作为Map的键
        int index = 2;
        int val = 1;
        //遍历数组，用花色+点数的组合,存储到Map集合中
        for(String number : numbers) {
            String poker;
            for(String color : colors) {
                poker = val+"_"+color + number;
                pooker.put(index, poker);
                pookerVal.put(index,index);
                pookerNumber.add(index);
                index++;
            }
            val++;
        }
        pooker.put(0,"14_W");//小王
        pooker.put(1,"15-W");//大王

    }

}
