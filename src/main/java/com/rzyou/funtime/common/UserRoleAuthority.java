package com.rzyou.funtime.common;

public enum UserRoleAuthority {

    A_1(1,"设置房间")
    ,A_2(2,"关闭房间")
    ,A_3(3,"播放音乐")
    ,A_4(4,"抱麦")
    ,A_5(5,"封麦")
    ,A_6(6,"禁麦")
    ,A_7(7,"下麦")
    ,A_8(8,"踢出")
    ,A_9(9,"设为主持")
    ,A_10(10,"抽麦序")
    ,A_11(11,"解封")
    ,A_12(12,"解禁")
    ,A_13(13,"取消主持")
    ,A_14(14,"设置背景")
    ;
    private int value;
    private String desc;

    UserRoleAuthority(int value, String desc) {
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
