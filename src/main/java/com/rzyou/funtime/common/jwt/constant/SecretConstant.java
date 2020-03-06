package com.rzyou.funtime.common.jwt.constant;

/**
 * @Author: Helon
 * @Description: JWT使用常量值
 * @Data: Created in 2018/7/27 14:37
 * @Modified By:
 */
public class SecretConstant {

    //签名秘钥
    public static final String BASE64SECRET = "ZW]4l5JH[m6Lm)LaQEjpb!4ElRaG(";

    //超时毫秒数（默认30天）
    public static final long EXPIRESSECOND = 30*24*60*60*1000L;

    //用于JWT加密的密匙
    public static final String DATAKEY = "u^3y6SPER4jm*fn";

}
