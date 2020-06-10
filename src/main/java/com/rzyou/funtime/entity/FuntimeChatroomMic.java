package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeChatroomMic implements Serializable {
    private static final long serialVersionUID = 6101504695021923217L;
    private Long id;

    private Long roomId;

    private Integer micLocation;

    private Long micUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;

    private Integer state;

    private Integer userRole;

    private Integer musicAuth;

}