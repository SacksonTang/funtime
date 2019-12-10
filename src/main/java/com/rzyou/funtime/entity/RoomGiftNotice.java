package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoomGiftNotice implements Serializable {

    private static final long serialVersionUID = -1675937780597566708L;
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
    public Integer type;//通知类型
    public String gid;//送的礼物
    public String rid;//房间ID
    public String giftImg;//礼物图片url
    public String fromUid;//送礼人uid
    public String fromImg;//送礼人头像
    public String fromName;//送礼人昵称
    public String toUid;//被送人uid
    public String toImg;//被送人头像
    public String toName;//被送人昵称

}
