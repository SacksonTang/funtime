package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomNotice implements Serializable {
    private static final long serialVersionUID = 6420662223991327559L;
    /**
     * 群发消息
     * 1 上麦
     * 2 下麦
     * 3 封麦
     * 4 解封
     * 5 禁麦
     * 6 解禁
     * 7 解散房间
     * 8 送礼
     * 9 全服通知礼物
     * 10 摇塞子
     * 11 普通公屏消息
     * 12 进入房间
     * 13 发红包
     * 14 表情图片
     *
     *
     * 单发消息
     * 15 抱麦
     * 16 踢人
     */
    public int type;
    public String msg;// 公屏消息内容
    public String name;// 发送人昵称
    public Integer roomId;//房间ID
    public int pos;// 上麦位置 0-7    8 右(右上) 9 左(房主)
    public String uid;// 发送人 uid
    public String imageUrl;//图片URL

    public int randomImage;// 摇塞子 1-9  显示的图片
}
