package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FuntimeBoxConf implements Serializable {
    private static final long serialVersionUID = 8355593783621196534L;
    private Integer boxNumber;
    private Integer drawId;
    private Integer probability;
    private Integer broadcast;
}
