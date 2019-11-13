package com.rzyou.funtime.common;

public enum PayState {

    START(1,"待支付"),PAYING(2,"支付中"),PAIED(3,"支付完成"),FAIL(4,"支付失败"),INVALID(5,"失效");
    private Integer value;
    private String desc;

    PayState(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
