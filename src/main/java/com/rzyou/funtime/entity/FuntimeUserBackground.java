package com.rzyou.funtime.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 2020/3/13
 * LLP-LX
 */
@Data
public class FuntimeUserBackground {
    private Long id;
    private Long userId;
    private Integer backgroundId;
    private Integer backgroundType;
    private BigDecimal price;
    private Integer months;
    private Date endTime;

}
