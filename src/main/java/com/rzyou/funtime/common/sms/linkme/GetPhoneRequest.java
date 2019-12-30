package com.rzyou.funtime.common.sms.linkme;

import lombok.Data;

import java.io.Serializable;
@Data
public class GetPhoneRequest implements Serializable {
    private static final long serialVersionUID = 5178270271037702850L;
    private String app_key;
    private String auth_code;
    private String channel;
    private String platform;
    private String token;
    private String sign;


    public GetPhoneRequest(String app_key, String channel, String platform, String token, String code,String sign) {
        this.app_key = app_key;
        this.token = token;
        this.channel = channel;
        this.platform = platform;
        this.auth_code = code;
        this.sign = sign;
    }

    public GetPhoneRequest(String app_key,  String channel, String platform,String token,String sign) {
        this.app_key = app_key;
        this.token = token;
        this.channel = channel;
        this.platform = platform;
        this.sign = sign;
    }
}
