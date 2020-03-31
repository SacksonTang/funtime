package com.rzyou.funtime.common;

/**
 * 2020/3/31
 * LLP-LX
 */
public enum LevelWealthType {
    RECHARGE(1,"APP充值"),CONVERT(2,"红钻兑换蓝钻"),ADMINPLUS(3,"后台增加"),ADMINSUB(4,"后台减少");
    private int value;
    private String desc;

    LevelWealthType(int value, String desc) {
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
