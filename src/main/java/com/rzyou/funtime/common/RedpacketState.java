package com.rzyou.funtime.common;

public enum RedpacketState {

    START(1,"领取中"),SUCCESS(2,"领取完成"),INVALID(3,"失效");
    private int value;
    private String desc;

    RedpacketState(int value, String desc) {
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
