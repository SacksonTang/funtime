package com.rzyou.funtime.common.encryption;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.sms.linkme.SignAlgorithm;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
        /** RSA算法要求有一个可信任的随机数源 */
        SecureRandom secureRandom = new SecureRandom();

        /** 为RSA算法创建一个KeyPairGenerator对象 */
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        /** 利用上面的随机数据源初始化这个KeyPairGenerator对象 */
        generator.initialize(1024, secureRandom);
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
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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

    public static void main(String[] args) throws Exception{

        /*
        String decryptHex = decrypt("66DF0A54B113E4BC777FCD419FDDEA05EC5B18ABA400C3B2AAC369901C318C3636359725C9E1F4677BCCDBBB3165E753A6831A1D028FDB65C5959E915D99D2F77A1507E988E633959E7242EA0E60ACC450604AF2AA06431B0F70653DC307DFA9A0FF0F96C6C6AD5C6D7C294E64D1104C814260277CAE89D1BA74DC54B78447274CB22DFB5918474786DFBBAC9F4055B44159A626E1429D922E33266AFCEDE6983150BE563E3CF47526CFBCF688BC164D4178B4DEA9F953803ABF79589F9250A5659939041960240CAEC9F91BAAA09665F232F899135CE4870A4359CA8F689442C5A020D5EAB8557E0EFFE3AB40301C809E7066A87883600FA764199626E7CC04",
                Constant.SERVER_PRIVATE_KEY);
        System.out.println(decryptHex);*/

        String cpk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC8wngFgF3AgagTGG/gpHGpuEY3W8EDzww2qJagA5CxFLDy3QxbrDg0FX84Z8Isyp5Xt/FPpve4gcT5lC/xfIZabWbNZyVLbVTYEd2OJCzsyO1QD+cw/nN64Us264t0vlKXsX0gZLonBExTEBG7SmQ9rk4r14VDZIaMDXeap5QQkwIDAQAB";
        String cpk2 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALzCeAWAXcCBqBMYb+Ckcam4RjdbwQPPDDaolqADkLEUsPLdDFusODQVfzhnwizKnle38U+m97iBxPmUL/F8hlptZs1nJUttVNgR3Y4kLOzI7VAP5zD+c3rhSzbri3S+UpexfSBkuicETFMQEbtKZD2uTivXhUNkhowNd5qnlBCTAgMBAAECgYAj6/5bTaqJ0CLkP2pLHgBjhCYYsuVwjB6O6FaB7SvEB2QFU/veFlA72Tt0yIoza/vmRaMuvV+3RTrvJDxbgI8wokYrTrEi/FpysX3mwmlG9cRAM1OQWG0krhz+mS5rHa36PqF4v+F7xbSOOG78AymB3mvTgrxZOPyIqrfP71fWwQJBAPGmAmoyPLPEQgfOkd8b/88meXxuVFaPJBpMgTt21t4e0IVV3PFdRvuqdO0+2HfgiCFZJcBQJbPchNxO65xZ/98CQQDH+FhFcCZCEvyWm7TVPWXaLtLO1oyazo8IpMqUZAFNNrw9e63UXSDplx0W5NuNmR38/oIY2jpRJCovA667lzXNAkBfCLi+Zw90a2TEiTrZEbvjDfCNPR6yBA1gwmG4rx4FsPy003XW/qYh54GgpCyhyI4A/3xXS63oNHuT4P3MOaDlAkByaCRgfdx1mxVGOYlJItp97KIRUnCZNSpiQJ3iLiEMgw9JgdQfFrT4z8sFySPPrFWa5CWcgGYy/CTiRxIWI1nVAkEAgCOUl/9h36nxd+TP0kTDqsCJNrpXQwdfAmPWogwry+PithJKUUWAd8uy0aoHS5XkXzHrY9R+rymqtSxlJkG7xw==";
        String encrypt = encryptHex(cpk, Constant.SERVER_PUBLIC_KEY);
        System.out.println("encrypt Str:"+encrypt);
        String encrypt1 = encryptHex(cpk, Constant.SERVER_PUBLIC_KEY);
        System.out.println("encrypt1 Str:"+encrypt1);
        encrypt = "19F5ADEBB276C22CF15B99E87CB5998A323731AFE6DE1EB6302B699642A01D6FC6438BAC85AC1EF96601096AFA21856BE8A39E597CE442F153815A28B09992725B289261C1D6E4BA3D23D7D8FA2D94910C634EAFB755BF84594E9AFCC78D2D060C94A193C14DBE88116B80255F1A4AB168FCECB39CF6CA9136446B6F140A6EA9";
        String decrypt = decryptHex(encrypt, cpk2);
        System.out.println("decrypt Str:"+decrypt);
        /*
        PublicKey publicKey = getPublicKey(cpk);
        System.out.println(Base64.encodeBase64String(publicKey.getEncoded()));
        KeyPair keyPair = getKeyPair();
        System.out.println("private key:"+byte2hex(keyPair.getPrivate().getEncoded()));
        System.out.println("public key:"+byte2hex(keyPair.getPublic().getEncoded()));
        String content = "1/1/1/11/1";
        //String encrypt = encryptHex(content, Constant.SERVER_PUBLIC_KEY);
        String encrypt = encryptHex(content, cpk);
        System.out.println("encrypt Str:"+encrypt);
        //String decrypt = decryptHex(encrypt, Constant.SERVER_PRIVATE_KEY);
        String decrypt = decryptHex(encrypt, cpk2);
        System.out.println("decrypt Str:"+decrypt);*/

    }

}