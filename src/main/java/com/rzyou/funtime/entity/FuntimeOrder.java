package com.rzyou.funtime.entity;

import lombok.Data;

@Data
public class FuntimeOrder {
    
    private Long userId;

    private String portraitAddress;

    private String nickname;

    private String orderTime;

    private String startHour;

    private String endHour;

    private String serviceTag;

    private String tagText;

    private String game;

    private Integer priceType;

    private String price;

    private Integer orderState;

    private Integer recommendation;

    private String serviceText;

}