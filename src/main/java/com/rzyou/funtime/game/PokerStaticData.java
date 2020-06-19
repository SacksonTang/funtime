package com.rzyou.funtime.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerStaticData {
    //创建Map集合，键：编号     值：牌
    public static Map<Integer,String> game21PokerMap = new HashMap<>();
    public static Map<Integer,String> pokerMap = new HashMap<>();

    //牌值对
    public static Map<String,Integer> game21pokerVal = new HashMap<>();
    public static Map<String,Integer> pokerVal = new HashMap<>();

    //创建List集合，存储编号
    public static List<Integer> game21PokerNumber = new ArrayList<>();
    public static List<Integer> pokerNumber = new ArrayList<>();
    //定义13个点数的数组
    public static String[] game21Numbers = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
    public static String[] numbers = {"3","4","5","6","7","8","9","10","J","Q","K","A","2"};
    //定义4个花色组合
    public static String[] colors = {"H","S","C","D"};//H-红桃S-黑桃C-梅花D-方块

    static {
        //定义一个整数变量，作为Map的键
        int index = 2;
        int val = 1;
        //遍历数组，用花色+点数的组合,存储到Map集合中
        for(String number : game21Numbers) {
            String poker;
            for(String color : colors) {
                if (number.equals("J")||number.equals("Q")||number.equals("K")){
                    val = 10;
                }
                poker = val+"_"+color +"_"+ number;
                game21PokerMap.put(index, poker);
                pokerMap.put(index, poker);

                game21pokerVal.put(poker,val);
                pokerVal.put(poker,val);
                game21PokerNumber.add(index);
                pokerNumber.add(index);
                index++;
            }
            val++;
        }
        pokerMap.put(0,"14_W_W");//小王
        pokerMap.put(1,"15-W_W");//大王
        pokerNumber.add(0);
        pokerNumber.add(1);
        pokerVal.put("14_W_W",val++);
        pokerVal.put("15_W_W",val++);

    }

    public static void main(String[] args) {
        System.out.println(game21PokerNumber);
        System.out.println(game21pokerVal);
        System.out.println(game21PokerMap);
    }


}
