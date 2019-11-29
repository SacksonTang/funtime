package com.rzyou.funtime.jwt.util;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.jwt.constant.SecretConstant;
import com.rzyou.funtime.jwt.secret.AESSecretUtil;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: JWT工具类
 * 参考官网：https://jwt.io/
 * 实现机制参考博客：https://www.cnblogs.com/tyrion1990/p/8134384.html
 * JWT的数据结构为：A.B.C三部分数据，由字符点"."分割成三部分数据
 * A-header头信息
 * B-payload 有效负荷 一般包括：已注册信息（registered claims），公开数据(public claims)，私有数据(private claims)
 * C-signature 签名信息 是将header和payload进行加密生成的
 * @Modified By:
 */
public class JwtHelper {

    private static Logger logger = LoggerFactory.getLogger(JwtHelper.class);

    /**
     * @Description: 生成JWT字符串
     * 格式：A.B.C
     * A-header头信息
     * B-payload 有效负荷
     * C-signature 签名信息 是将header和payload进行加密生成的
     * @param userId - 用户编号
     * @Modified By:
     */
    public static String generateJWT(String userId,String imei) {
        //签名算法，选择SHA-256
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //获取当前系统时间
        long nowTimeMillis = System.currentTimeMillis();
        Date now = new Date(nowTimeMillis);
        //将BASE64SECRET常量字符串使用base64解码成字节数组
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SecretConstant.BASE64SECRET);
        //使用HmacSHA256签名算法生成一个HS256的签名秘钥Key
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        //添加构成JWT的参数
        Map<String, Object> headMap = new HashMap<>();

        headMap.put("alg", SignatureAlgorithm.HS256.getValue());
        headMap.put("typ", "JWT");
        JwtBuilder builder = Jwts.builder().setHeader(headMap)
                //加密后的客户ID
                .claim("userId", AESSecretUtil.encryptToStr(userId, SecretConstant.DATAKEY))
                .claim("imei",AESSecretUtil.encryptToStr(imei, SecretConstant.DATAKEY))
                //Signature
                .signWith(signatureAlgorithm, signingKey);
        //添加Token过期时间
        if (SecretConstant.EXPIRESSECOND >= 0) {
            long expMillis = nowTimeMillis + SecretConstant.EXPIRESSECOND;
            Date expDate = new Date(expMillis);
            builder.setExpiration(expDate).setNotBefore(now);
        }
        return builder.compact();
    }

    /**
     * @Description: 解析JWT
     * 返回Claims对象
     * @param jsonWebToken - JWT
     * @Modified By:
     */
    public static Claims parseJWT(String jsonWebToken) {
        Claims claims = null;
        try {
            if (StringUtils.isNotBlank(jsonWebToken)) {
                //解析jwt
                claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SecretConstant.BASE64SECRET))
                        .parseClaimsJws(jsonWebToken).getBody();
                return claims;
            } else {
                logger.error("[JWTHelper]-json web token 为空");
                throw new BusinessException(ErrorMsgEnum.USER_TOKEN_EMPTY.getValue(),ErrorMsgEnum.USER_TOKEN_EMPTY.getDesc());

            }
        }catch (ExpiredJwtException je){
            logger.error("[JWTHelper]-JWT解析异常：token已经超时");
            throw new BusinessException(ErrorMsgEnum.USER_TOKEN_EXPIRE.getValue(),ErrorMsgEnum.USER_TOKEN_EXPIRE.getDesc());

        } catch (Exception e) {
            logger.error("[JWTHelper]-JWT解析异常：可能因为token已经超时或非法token");
            throw new BusinessException(ErrorMsgEnum.USER_TOKEN_ERROR.getValue(),ErrorMsgEnum.USER_TOKEN_ERROR.getDesc());

        }

    }

    /**
     * @Description: 校验JWT是否有效
     * @param jsonWebToken - JWT
     * @Modified By:
     */
    public static Map<String,Object> validateLogin(String jsonWebToken) {
        Claims claims = parseJWT(jsonWebToken);

        if (claims != null) {
            //解密客户编号
            String decryptUserId = AESSecretUtil.decryptToStr((String)claims.get("userId"), SecretConstant.DATAKEY);
            String decryptIemi = AESSecretUtil.decryptToStr((String)claims.get("imei"), SecretConstant.DATAKEY);
            Map<String,Object> result = new HashMap<>();
            result.put("userId",decryptUserId);
            result.put("imei",decryptIemi);
            return result;
        }else {
            logger.warn("[JWTHelper]-JWT解析出claims为空");
            throw new BusinessException(ErrorMsgEnum.USER_TOKEN_EMPTY.getValue(),ErrorMsgEnum.USER_TOKEN_EMPTY.getDesc());
        }
    }

    public static void main(String[] args) {
       String jsonWebKey = generateJWT("123","11");
       System.out.println(jsonWebKey);
       Claims claims =  parseJWT(jsonWebKey);
       if(claims!=null) {
           System.out.println(claims.getNotBefore() + "---" + claims.getExpiration());
           System.out.println(claims);
           System.out.println(validateLogin(jsonWebKey));
       }
    }


}
