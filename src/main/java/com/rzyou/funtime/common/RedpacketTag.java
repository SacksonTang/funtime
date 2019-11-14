package com.rzyou.funtime.common;

public enum  RedpacketTag {
    SORT_1(1,"首榜"),SORT_2(2,"运气王"), SORT_3(3,"小幸运"),SORT_4(4,"小可怜");
    private int value;
    private String desc;

    RedpacketTag(int value, String desc) {
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
