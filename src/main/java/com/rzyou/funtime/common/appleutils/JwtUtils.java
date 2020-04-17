package com.rzyou.funtime.common.appleutils;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.httputil.HttpClientUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;


public class JwtUtils {

    private static String getAppleIdPublicKeyFromRemote() {

        return HttpClientUtil.doGet(Constant.APPLE_GET_KEYS);
    }

    private static PublicKey getAppleIdPublicKey(String kid) {
        String publicKeyStr = getAppleIdPublicKeyFromRemote();
        return publicKeyAdapter(publicKeyStr, kid);
    }

    /**
     * 将appleServer返回的publicKey转换成PublicKey对象
     *  
     *
     * @param publicKeyStr
     * @return
     */
    private static PublicKey publicKeyAdapter(String publicKeyStr, String kid) {
        if (!StringUtils.hasText(publicKeyStr)) {
            return null;
        }
        Map maps = (Map) JSON.parse(publicKeyStr);
        List<Map> keys = (List<Map>) maps.get("keys");
        Map o = null;
        for (Map key : keys) {
            if (kid.equals(key.get("kid"))) {
                o = key;
                break;
            }
        }
        if (null != o) {
            Jwk jwa = Jwk.fromValues(o);
            try {
                return jwa.getPublicKey();
            } catch (InvalidPublicKeyException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

    }

    private static boolean verify2(PublicKey key, String jwt, String audience, String subject) {
        JwtParser jwtParser = Jwts.parser().setSigningKey(key);
        jwtParser.requireIssuer("https://appleid.apple.com");
        jwtParser.requireAudience(audience);
        jwtParser.requireSubject(subject);
        try {
            Jws<Claims> claim = jwtParser.parseClaimsJws(jwt);
            return claim != null && claim.getBody().containsKey("auth_time");
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isValid(String accessToken) {
        CusJws cusJws = getJws(accessToken);
        if (cusJws == null) {
            return false;
        }
        // iss
        long curTime = System.currentTimeMillis();
        if (cusJws.getJwsPayload().getExp() * 1000 < curTime) {
            return false;
        }
        if (!JwsPayload.ISS.equals(cusJws.getJwsPayload().getIss())) {
            return false;
        }
        // 校验签名
        PublicKey publicKey = getAppleIdPublicKey(cusJws.getJwsHeader().getKid());

        return verify2(publicKey, accessToken, cusJws.getJwsPayload().getAud(), cusJws.getJwsPayload().getSub());
    }


    private static CusJws getJws(String identityToken) {
        String[] arrToken = identityToken.split("\\.");
        if (arrToken.length != 3) {
            return null;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        JwsHeader jwsHeader = JSON.parseObject(new String(decoder.decode(arrToken[0])), JwsHeader.class);
        JwsPayload jwsPayload = JSON.parseObject(new String(decoder.decode(arrToken[1])), JwsPayload.class);
        return new CusJws(jwsHeader, jwsPayload, arrToken[2]);
    }

    static class CusJws {
        private JwsHeader jwsHeader;
        private JwsPayload jwsPayload;
        private String signature;

        public CusJws(JwsHeader jwsHeader, JwsPayload jwsPayload, String signature) {
            this.jwsHeader = jwsHeader;
            this.jwsPayload = jwsPayload;
            this.signature = signature;
        }

        public JwsHeader getJwsHeader() {
            return jwsHeader;
        }

        public void setJwsHeader(JwsHeader jwsHeader) {
            this.jwsHeader = jwsHeader;
        }

        public JwsPayload getJwsPayload() {
            return jwsPayload;
        }

        public void setJwsPayload(JwsPayload jwsPayload) {
            this.jwsPayload = jwsPayload;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }

    static class JwsHeader {
        private String kid;
        private String alg;

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }
    }

    static class JwsPayload {
        private String iss;
        private String sub;
        private String aud;
        private long exp;
        private long iat;
        private String nonce;
        private String email;
        private boolean email_verified;

        public final static String ISS = "https://appleid.apple.com";

        public String getIss() {
            return iss;
        }

        public void setIss(String iss) {
            this.iss = iss;
        }

        public String getSub() {
            return sub;
        }

        public void setSub(String sub) {
            this.sub = sub;
        }

        public String getAud() {
            return aud;
        }

        public void setAud(String aud) {
            this.aud = aud;
        }

        public long getExp() {
            return exp;
        }

        public void setExp(long exp) {
            this.exp = exp;
        }

        public long getIat() {
            return iat;
        }

        public void setIat(long iat) {
            this.iat = iat;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isEmail_verified() {
            return email_verified;
        }

        public void setEmail_verified(boolean email_verified) {
            this.email_verified = email_verified;
        }
    }
}
