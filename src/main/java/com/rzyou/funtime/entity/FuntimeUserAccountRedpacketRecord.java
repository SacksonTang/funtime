package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeUserAccountRedpacketRecord implements Serializable {
    private static final long serialVersionUID = -6288614660477466331L;
    private Long id;

    private Long userId;

    private Long detailId;

    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date sendTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Integer tagId;

    private String orderNo;

    private Long giftRecordId;

    private String nickname;

    private String portraitAddress;

    private String tagName;
}