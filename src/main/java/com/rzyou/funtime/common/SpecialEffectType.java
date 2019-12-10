package com.rzyou.funtime.common;

public enum SpecialEffectType {
    E_1(1,"坑上头像直送")
    ,E_2(2,"房间顶部滑动")
    ,E_3(3,"房间全屏")
    ,E_4(4,"所有房间全屏")

    ;
    private int value;
    private String desc;

    SpecialEffectType(int value, String desc) {
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
