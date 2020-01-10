package com.rzyou.funtime.common;

public enum SmsType {
    REGISTER_LOGIN(1,"注册登录")
    ,UPDATE_PHONENUMBER(2,"修改手机号")
    ,REAL_VALID(3,"实名认证")
    ;
    private int value;
    private String desc;

    SmsType(int value, String desc) {
        this.value = value;
        this.desc = desc;
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
