package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 2020/2/28
 * LLP-LX
 */
@Data
public class FuntimeGameYaoyaoConf implements Serializable {

    private Integer id;
    private Integer number1;
    private Integer number2;
    private Integer number3;
    private Integer drawType;
    private BigDecimal drawVal;
    private Integer probability;
    private Integer broadcast;
}
