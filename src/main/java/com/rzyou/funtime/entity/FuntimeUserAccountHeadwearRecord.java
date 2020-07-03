package com.rzyou.funtime.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 2020/3/13
 * LLP-LX
 */
@Data
public class FuntimeUserAccountHeadwearRecord {
    private Long id;
    private Long userId;
    private Integer headwearId;
    private BigDecimal price;
    private Integer days;

}
