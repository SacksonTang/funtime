package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeChatroom implements Serializable {
    private static final long serialVersionUID = -2515077135331000276L;
    private Long id;

    private Long userId;

    private String password;

    private String name;

    private String examUrl;

    private String avatarUrl;

    private String tags;

    private String examDesc;

    private Integer isBlock;

    private Integer isLock;

    private Integer hot;

    private Integer onlineNum;

    private Integer state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private String chatMessageFile;

    private String portraitAddress;

    private String nickname;

    private Integer sex;

    private Long showId;
}