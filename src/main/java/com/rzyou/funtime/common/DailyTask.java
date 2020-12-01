package com.rzyou.funtime.common;

public enum  DailyTask {
    BLUE_BLACK(1,"蓝钻兑换黑钻")
    ,BLACK_BLUE(2,"黑钻兑换蓝钻")
    ,BLACK_RMB(3,"黑钻兑换人民币")
    ,RMB_BLUE(4,"人民币兑换蓝钻")
    ,BLUE_GOLD(5,"蓝钻兑换金币")
    ;
    private int value;
    private String desc;

    DailyTask(int value, String desc) {
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
