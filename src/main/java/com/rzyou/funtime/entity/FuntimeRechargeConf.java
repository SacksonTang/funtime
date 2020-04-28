package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeRechargeConf implements Serializable {
    private static final long serialVersionUID = -507150281942903742L;
    private Integer id;

    private BigDecimal rechargeRmb;

    private BigDecimal rechargeNum;

    private BigDecimal convertibleRatio;

    private String tagColour;

    private String tagName;

    private Integer hornNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Integer platform;

    private String productId;

    private Integer goldNum;


}