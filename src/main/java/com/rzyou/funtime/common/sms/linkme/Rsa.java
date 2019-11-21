package com.rzyou.funtime.common.sms.linkme;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.TreeMap;

public class Rsa {

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 获取密钥对
     *
     * @return 密钥对
     */
    public static KeyPair getKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    /**
     * 获取私钥
     *
     * @param privateKey 私钥字符串
     * @return
     */
    private static PrivateKey getPrivateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = Base64.decodeBase64(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 获取公钥
     *
     * @param publicKey 公钥字符串
     * @return
     */
    private static PublicKey getPublicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = Base64.decodeBase64(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * RSA加密
     *
     * @param data 待加密数据
     * @param publicKey 公钥
     * @return
     */
    public static String encrypt(String data, String publicKey) throws Exception {
        byte[] encryptedData = encrypt(data.getBytes(), publicKey);
        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
        // 加密后的字符串
        return Base64.encodeBase64String(encryptedData);
    }

    public static String encryptHex(String data, String publicKey) throws Exception {
        byte[] encryptedData = encrypt(data.getBytes(), publicKey);
        // 加密内容为16进制
        return byte2hex(encryptedData);
    }

    public static byte[] encrypt(byte[] data, String publicKey) throws Exception {
        PublicKey pubKey = getPublicKey(publicKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        // 加密后的字节
        return encryptedData;
    }



    /**
     * RSA解密
     *
     * @param data 待解密数据
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String data, String privateKey) throws Exception {
        byte[] decryptedData = decrypt(Base64.decodeBase64(data), privateKey);
        // 解密后的内容
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static String decryptHex(String data, String privateKey) throws Exception {
        byte[] decryptedData = decrypt(hexStr2byte(data), privateKey);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static byte[] decrypt(byte[] data, String privateKey) throws Exception {
        PrivateKey priKey = getPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(data, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        // 解密后的字节内容
        return decryptedData;
    }

    /**
     * 签名
     *
     * @param data 待签名数据
     * @param privateKey 私钥
     * @return 签名
     */
    public static String sign(String data, String privateKey, SignAlgorithm signAlgorithm) throws Exception {
        byte[] signData = sign(data.getBytes(), privateKey, signAlgorithm);
        return Base64.encodeBase64String(signData);
    }

    public static String signHex(String data, String privateKey, SignAlgorithm signAlgorithm) throws Exception {
        byte[] signData = sign(data.getBytes(), privateKey, signAlgorithm);
        return byte2hex(signData);
    }

    public static byte[] sign(byte[] data, String privateKey, SignAlgorithm signAlgorithm) throws Exception {
        PrivateKey priKey = getPrivateKey(privateKey);
        Signature signature = Signature.getInstance(signAlgorithm.getAlgorithm());
        signature.initSign(priKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * 验签
     *
     * @param srcData 原始字符串
     * @param publicKey 公钥
     * @param sign 签名
     * @return 是否验签通过
     */
    public static boolean verify(String srcData, String publicKey, String sign, SignAlgorithm signAlgorithm) throws Exception {
        return verify(srcData.getBytes(), publicKey, Base64.decodeBase64(sign), signAlgorithm);
    }

    public static boolean verifyHex(String srcData, String publicKey, String sign, SignAlgorithm signAlgorithm) throws Exception {
        return verify(srcData.getBytes(), publicKey, hexStr2byte(sign), signAlgorithm);
    }

    public static boolean verify(byte[] srcData, String publicKey, byte[] sign, SignAlgorithm signAlgorithm) throws Exception {
        PublicKey pubKey = getPublicKey(publicKey);
        Signature signature = Signature.getInstance(signAlgorithm.getAlgorithm());
        signature.initVerify(pubKey);
        signature.update(srcData);
        return signature.verify(sign);
    }

    /**
     * Description：将二进制转换成16进制字符串
     *
     * @param b
     * @return
     * @return String
     * @author name：
     */
    private static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * Description：将十六进制的字符串转换成字节数据
     *
     * @param strhex
     * @return
     * @return byte[]
     * @author name：
     */
    private static byte[] hexStr2byte(String strhex) {
        if (strhex == null) {
            return null;
        }
        int l = strhex.length();
        if (l % 2 == 1) {
            return null;
        }
        byte[] b = new byte[l / 2];
        for (int i = 0; i != l / 2; i++) {
            b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }

}