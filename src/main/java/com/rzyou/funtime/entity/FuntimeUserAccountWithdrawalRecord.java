package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeUserAccountWithdrawalRecord implements Serializable {
    private static final long serialVersionUID = 8911459288167741645L;
    private Long id;

    private String orderNo;

    private Long userId;

    private Integer trialType;

    private Integer withdrawalType;

    private String cardNumber;

    private String thirdOrderNumber;

    private BigDecimal amount;

    private BigDecimal blackDiamond;

    private BigDecimal blackRmbRatio;

    private BigDecimal preRmbAmount;

    private BigDecimal channelAmount;

    private String reason;

    private Integer firstTrialId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date firstTrialTime;

    private Integer retrialId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date retrialTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Integer state;

    private Integer flag;

    private Long version;

    private String nickname;

}