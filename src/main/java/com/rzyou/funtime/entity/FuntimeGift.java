package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeGift implements Serializable {
    private static final long serialVersionUID = -6899314288032003355L;
    private Integer id;

    private Integer giftTagId;

    private String giftName;

    private BigDecimal originalPrice;

    private BigDecimal activityPrice;

    private Integer specialEffect;

    private Integer bestowed;

    private String animationType;

    private String animationUrl;

    private String imageUrl;

    private BigDecimal animationLength;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Integer sort;


}