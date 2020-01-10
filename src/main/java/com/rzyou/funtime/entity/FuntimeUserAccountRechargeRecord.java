package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeUserAccountRechargeRecord implements Serializable {
    private static final long serialVersionUID = 4544338349394994130L;
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private BigDecimal rmb;

    private String rechargeCardId;

    private Integer rechargeChannelId;

    private Integer rechargeConfId;

    private Integer hornNum;

    private String os;

    private String phoneType;

    private String imei;

    private Integer state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Long version;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date completeTime;

    private String orderNo;

    private String tagName;
}