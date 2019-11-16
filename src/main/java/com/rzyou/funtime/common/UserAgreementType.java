package com.rzyou.funtime.common;

public enum UserAgreementType {
    CONVERT_AGREEMENT(1,"兑换协议")
    ,USER_AGREEMENT(2,"用户协议")
    ,RECHARGE_AGREEMENT(3,"充值协议")
    ,PRIVACY_AGREEMENT(4,"隐私协议")

    ;
    private int value;
    private String desc;

    UserAgreementType(int value, String desc) {
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
