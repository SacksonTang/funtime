package com.rzyou.funtime.utils;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StringUtil {

    /**
     * 创建指定数量的随机字符串
     *
     * @param numberFlag 是否是数字
     * @param length
     * @return
     */
    public static String createRandom(boolean numberFlag, int length) {
        String retStr = "";
        String strTable = numberFlag ? "1234567890" : "1234567890abcdefghijkmnpqrstuvwxyz";
        int len = strTable.length();
        boolean bDone = true;
        do {
            retStr = "";
            int count = 0;
            for (int i = 0; i < length; i++) {
                double dblR = Math.random() * len;
                int intR = (int) Math.floor(dblR);
                char c = strTable.charAt(intR);
                if (('0' <= c) && (c <= '9')) {
                    count++;
                }
                retStr += strTable.charAt(intR);
            }
            if (count >= 2) {
                bDone = false;
            }
        } while (bDone);

        return retStr;
    }

    public static String createNonceStr() {
        String s = UUID.randomUUID().toString();
        // 去掉“-”符号
        return s.replaceAll("\\-", "").toUpperCase();
    }

    public static String createOrderId(){
        String str = createRandom(true,4);
        String currency =  DateUtil.getCurrentDateTime();
        return currency+str;
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
       for (int i = 0;i<501;i++){
           list.add(i);
       }
        System.out.println(list.size());
        int size = list.size();
        int fromIndex = 0;
        int toIndex = 500;
        int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
        for (int j = 1;j<k+1;j++){
            List<Integer> spList = list.subList(fromIndex,toIndex);
            System.out.println(spList.size());
            fromIndex = toIndex;
            toIndex =  (j+1)*toIndex > size ? size : (j+1)*toIndex;
        }
    }

}
