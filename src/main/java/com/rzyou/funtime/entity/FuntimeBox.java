package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FuntimeBox implements Serializable {
    private static final long serialVersionUID = 2437816330797798730L;
    private Integer boxNumber;
    private String boxName;
    private BigDecimal originalPrice;
    private BigDecimal activityPrice;
    private String animationType;
    private String animationUrl;
    private String imageUrl;
    private BigDecimal price;
}
