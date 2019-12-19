package com.rzyou.funtime.common.sms.linkme;

public class GetPhoneRequest {
    private String app_key;
    private String token;
    private String channel;
    private String platform;
    private String code;
    private String sign;


    public GetPhoneRequest(String app_key, String token, String channel, String platform, String code,String sign) {
        this.app_key = app_key;
        this.token = token;
        this.channel = channel;
        this.platform = platform;
        this.code = code;
        this.sign = sign;
    }

    public GetPhoneRequest(String app_key, String token, String channel, String platform,String sign) {
        this.app_key = app_key;
        this.token = token;
        this.channel = channel;
        this.platform = platform;
        this.sign = sign;
    }
}
