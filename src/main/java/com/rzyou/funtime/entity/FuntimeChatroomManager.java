package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeChatroomManager implements Serializable {
    private static final long serialVersionUID = -1713021924205415937L;
    private Long id;
    private Long roomId;
    private Long userId;
    private Integer duration;
    private Date expireTime;
}
