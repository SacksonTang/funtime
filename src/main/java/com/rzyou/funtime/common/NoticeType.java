package com.rzyou.funtime.common;

public enum NoticeType {

    ROOM_MIC_UPPER(Constant.ROOM_MIC_UPPER,"上麦","")
    ,ROOM_MIC_LOWER(Constant.ROOM_MIC_LOWER,"下麦","")
    ,ROOM_MIC_STOP(Constant.ROOM_MIC_STOP,"封麦","")
    ,ROOM_MIC_OPEN(Constant.ROOM_MIC_OPEN,"解封","")
    ,ROOM_MIC_FORBID(Constant.ROOM_MIC_FORBID,"禁麦","")
    ,ROOM_MIC_RELEASE(Constant.ROOM_MIC_RELEASE,"解禁","")
    ,ROOM_CLOSE(Constant.ROOM_CLOSE,"解散房间","")
    ,ROOM_GIFT_SEND(Constant.ROOM_GIFT_SEND,"送礼","")
    ,ROOM_GIFT_SEND_ALL(Constant.ROOM_GIFT_SEND_ALL,"全服房间通知礼物","")
    ,ROOM_MIC_RANDOM(Constant.ROOM_MIC_RANDOM,"抽麦序","")
    ,ROOM_MSG_NORMAL(Constant.ROOM_MSG_NORMAL,"普通公屏消息","")
    ,ROOM_ENTER(Constant.ROOM_ENTER,"进入房间","")
    ,ROOM_REDPACKET_SEND(Constant.ROOM_REDPACKET_SEND,"发红包","")
    ,ROOM_EXPRESSION(Constant.ROOM_EXPRESSION,"表情图片","")
    ,ROOM_MIC_HOLDING(Constant.ROOM_MIC_HOLDING,"抱麦","")
    ,ROOM_KICKED(Constant.ROOM_KICKED,"踢人","")
    ,ROOM_MANAGE(Constant.ROOM_MANAGE,"设为主持","")
    ,ROOM_MANAGE_CANCEL(Constant.ROOM_MANAGE_CANCEL,"设为主持","")
    ,ROOM_GIFT_SEND_ROOM(Constant.ROOM_GIFT_SEND_ROOM,"全房送礼","")
    ,ROOM_USER_COUNT(Constant.ROOM_USER_COUNT,"房间人数","")
    ,ROOM_GIFT_SEND_ROOM_ALL(Constant.ROOM_GIFT_SEND_ROOM_ALL,"全房送超级大礼","")
    ,SERVICE_MSG(Constant.SERVICE_MSG,"全服大喇叭","")
    ;
    private int value;
    private String desc;
    private String msg;

    NoticeType(int value, String desc,String msg) {
        this.value = value;
        this.desc = desc;
        this.msg = msg;
    }

    public static String getMsgByVal(int val){
        for (NoticeType noticeType : NoticeType.values()){
            if (noticeType.value == val){
                return noticeType.msg;
            }
        }
        return null;
    }


    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
