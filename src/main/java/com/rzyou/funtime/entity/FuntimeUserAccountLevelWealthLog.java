package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class FuntimeUserAccountLevelWealthLog implements Serializable {
    private Long id;

    private Long userId;

    private Long relationId;

    private String actionType;

    private String operationType;

    private Integer levelVal;

    private Integer wealthVal;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;


}