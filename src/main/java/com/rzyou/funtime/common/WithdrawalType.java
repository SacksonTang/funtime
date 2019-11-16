package com.rzyou.funtime.common;

public enum WithdrawalType {

    DESPOSIT_CARD(1,"银行卡领赏")
    ,WXPAY(2,"微信领赏")
    ,ALIPAY(3,"支付宝领赏")

    ;
    private int value;
    private String desc;

    WithdrawalType(int value, String desc) {
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
