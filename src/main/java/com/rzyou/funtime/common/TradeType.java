package com.rzyou.funtime.common;

/**
 * 2020/2/19
 * LLP-LX
 */
public enum  TradeType {
    JSAPI("JSAPI","JSAPI支付(或小程序支付)")
    ,NATIVE("NATIVE","Native支付")
    ,APP("APP","app支付")
    ,MWEB("MWEB","H5支付")
    ,APPLE("APPLE","苹果内购")
            ;
    private String value;
    private String desc;

    TradeType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
