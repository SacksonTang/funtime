package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 2020/3/5
 * LLP-LX
 */
@Data
public class FuntimeUserAccountCircleRecord implements Serializable {

    private static final long serialVersionUID = -3931410555634458353L;
    private Long id;
    private Integer activityId;
    private Long userId;
    private Long roomId;
    private Integer blueAmount;
    private Integer drawRandom;
    private Integer drawNumber;
    private Integer drawType;
    private Integer drawId;
    private BigDecimal drawVal;
}
