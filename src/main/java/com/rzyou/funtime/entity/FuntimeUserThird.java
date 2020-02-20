package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeUserThird implements Serializable {
    private static final long serialVersionUID = -1777813209273139995L;
    private Long id;

    private Long userId;

    private String thirdType;

    private String openid;

    private String unionid;

    private Date createTime;

    private String token;

    private String nickname;
}