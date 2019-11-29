package com.rzyou.funtime.common;

public enum UserRole {

    ROOM_CREATER(1,"房主")
    ,ROOM_CHAIR(2,"主持")
    ,ROOM_MIC(3,"普通麦位")
    ,ROOM_NORMAL(4,"普通用户")

    ;
    private int value;
    private String desc;

    UserRole(int value, String desc) {
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
