package com.rzyou.funtime.common;

public enum GiveChannel {

    ROOM(1,"房间"),TALK(2,"对话"),REDPACKET(3,"红包赠送");
    private int value;
    private String desc;

    GiveChannel(int value, String desc) {
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
