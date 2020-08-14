package com.rzyou.funtime.utils;


import org.apache.commons.codec.digest.DigestUtils;

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
        System.out.println(DigestUtils.sha1Hex("00A79B14-5B55-4052-83D2-CDB8D4C0CC5B"));
        System.out.println(DigestUtils.sha1Hex("64322AA0-2B79-403C-AF2F-79B867FB9251"));
        System.out.println(DigestUtils.sha1Hex("6A47E279-2637-4ABF-BAB0-4A6CC0A59D4E"));
    }

}
