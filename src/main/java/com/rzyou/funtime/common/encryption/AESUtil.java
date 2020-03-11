package com.rzyou.funtime.common.encryption;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
    public static final String VIPARA = "9769475569322011";
    public static final String bm = "utf-8";

    /**
     * 字节数组转化为大写16进制字符串
     *
     * @param b
     * @return
     */
    private static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String s = Integer.toHexString(b[i] & 0xFF);
            if (s.length() == 1) {
                sb.append("0");
            }

            sb.append(s.toUpperCase());
        }

        return sb.toString();
    }

    /**
     * 16进制字符串转字节数组
     *
     * @param s
     * @return
     */
    private static byte[] str2ByteArray(String s) {
        int byteArrayLength = s.length() / 2;
        byte[] b = new byte[byteArrayLength];
        for (int i = 0; i < byteArrayLength; i++) {
            byte b0 = (byte) Integer.valueOf(s.substring(i * 2, i * 2 + 2), 16)
                    .intValue();
            b[i] = b0;
        }

        return b;
    }


    /**
     * AES 加密
     *
     * @param content
     *            明文
     * @param key
     *            生成秘钥的关键字
     * @return
     */

    public static String aesEncrypt(String content, String key) {
        try {
            IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, zeroIv);
            byte[] encryptedData = cipher.doFinal(content.getBytes(bm));

            return byte2HexStr(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ENCRYPT_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ENCRYPT_ERROR.getDesc());
        }
    }

    /**
     * AES 解密
     *
     * @param content
     *            密文
     * @param key
     *            生成秘钥的关键字
     * @return
     */

    public static String aesDecrypt(String content, String key) {
        try {
            byte[] byteMi=  str2ByteArray(content);
            IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, zeroIv);
            byte[] decryptedData = cipher.doFinal(byteMi);
            return new String(decryptedData, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.PARAMETER_DECRYPT_ERROR.getValue(),ErrorMsgEnum.PARAMETER_DECRYPT_ERROR.getDesc());
        }
    }

    public static void main(String[] args) {
        String key = "Yhi6HglhWHBiw0ZQ";
        String encrypt = aesEncrypt("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxMjMiLCJ1c2VyTmFtZSI6Ikp1ZHkiLCJleHAiOjE1MzI3Nzk2MjIsIm5iZiI6MTUzMjc3NzgyMn0.sIw_leDZwG0pJ8ty85Iecd_VXjObYutILNEwPUyeVSo", key);
        System.out.println(encrypt);
        System.out.println(aesDecrypt(encrypt,key));
    }
}
