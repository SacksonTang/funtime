package com.rzyou.funtime.entity;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class FuntimeGameSmashEggConf {

    private Integer id;
    private Integer drawNumber;
    private String drawUrl;
    private Integer type;
    private Integer drawType;
    private Integer drawId;
    private BigDecimal drawVal;
    private Integer probability;
    private Integer broadcast;

}
