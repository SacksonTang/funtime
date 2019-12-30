package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeUserAccountGifttransRecord implements Serializable {
    private static final long serialVersionUID = 4497399804466483363L;
    private Long id;

    private Long userId;

    private String actionType;

    private String operationType;

    private String operationDesc;

    private BigDecimal amount;

    private Integer num;

    private Integer giftId;

    private String giftName;

    private Long toUserId;

    private Integer giveChannelId;

    private Integer state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Long version;

    private Date completeTime;

    private String orderNo;

    private String nickname;

    private String animationType;

    private String animationUrl;

    private String imageUrl;

    private Integer concerned;

}