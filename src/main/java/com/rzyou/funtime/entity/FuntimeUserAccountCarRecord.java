package com.rzyou.funtime.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 2020/3/13
 * LLP-LX
 */
@Data
public class FuntimeUserAccountCarRecord {
    private Long id;
    private Long userId;
    private Integer carId;
    private BigDecimal price;
    private Integer days;

}
