package com.rzyou.funtime.common.sms.linkme;

import lombok.Data;

@Data
public class SmsSendRequest {

    private String app_key;

    private String recipient;

    private String sign_name;

    private String template_id;

    private String[] template_params;

    private String status_callback_url;

    private String sign;

    private String extend;

    public SmsSendRequest(String app_key, String recipient, String sign_name, String template_id, String[] template_params, String status_callback_url, String sign, String extend) {
        this.app_key = app_key;
        this.recipient = recipient;
        this.sign_name = sign_name;
        this.template_id = template_id;
        this.template_params = template_params;
        this.status_callback_url = status_callback_url;
        this.sign = sign;
        this.extend = extend;
    }
}
