package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.PrimitiveIterator;
@Data
public class FuntimeUserRedpacket implements Serializable {
    private static final long serialVersionUID = 5745405731257204305L;
    private Long id;

    private Integer type;

    private Long userId;

    private Long toUserId;

    private Integer redpacketNum;

    private BigDecimal amount;

    private String redpacketDesc;

    private Integer bestowCondition;

    private Integer giftId;

    private Integer state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date invalidTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private BigDecimal grabAmount;

    private Integer grabNum;

    private String animationType;

    private String animationUrl;

    private Long roomId;

    private String nickname;

    private String portraitAddress;

    private String giftName;

    private String imageUrl;

    private Integer sex;

}