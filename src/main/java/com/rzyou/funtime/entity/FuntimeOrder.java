package com.rzyou.funtime.entity;

import lombok.Data;

@Data
public class FuntimeOrder {
    
    private Long userId;

    private String orderTime;

    private String startHour;

    private String endHour;

    private String serviceTag;

    private String tagText;

    private Integer priceType;

    private Integer price;

    private Integer orderState;

    private Integer recommendation;

    private String serviceText;

}