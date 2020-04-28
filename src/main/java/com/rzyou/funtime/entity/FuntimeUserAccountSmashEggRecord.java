package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 2020/3/5
 * LLP-LX
 */
@Data
public class FuntimeUserAccountSmashEggRecord implements Serializable {

    private static final long serialVersionUID = 7800714632749617351L;
    private Long id;
    private Long userId;
    private Integer blueAmount;
    private Integer drawRandom;
    private Integer drawNumber;
    private Integer type;
    private Integer drawType;
    private Integer drawId;
    private BigDecimal drawVal;
}
