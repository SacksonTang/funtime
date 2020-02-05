package com.rzyou.funtime.common;

public enum OuyiSmsTemplate {

    REGISTER(SmsType.REGISTER_LOGIN.getValue(),"登陆注册验证码 : #，为保证账号安全，请勿泄露给他人【欢时科技】"),
    REAL_VALID(SmsType.REAL_VALID.getValue(),"实名认证验证码 : #，为保证账号安全，请勿泄露给他人【欢时科技】"),
    UPDATE_PHONENUMBER(SmsType.UPDATE_PHONENUMBER.getValue(),"修改手机号验证码 : #，为保证账号安全，请勿泄露给他人【欢时科技】"),
    BIND_PHONENUMBER(SmsType.BIND_PHONENUMBER.getValue(),"绑定手机号验证码 : #，为保证账号安全，请勿泄露给他人【欢时科技】");
    private int value;
    private String desc;

    OuyiSmsTemplate(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static String getDescByValue(int val){
        for (OuyiSmsTemplate template:OuyiSmsTemplate.values()){
            if (template.value==val){
                return template.desc;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
