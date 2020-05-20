package com.rzyou.funtime.common;

public enum GameCodeEnum {
    YAOYAOLE(1001,"摇摇乐"),FISH(1002,"捕鱼"),EGG(1003,"砸蛋"),CIRCLE(1004,"夺宝");
    private int value;
    private String desc;

    GameCodeEnum(int value, String desc) {
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
