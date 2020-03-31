package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class FuntimeUserAccountLevelWealthRecord implements Serializable {
    private Long id;
    private Long userId;
    private Integer levelVal;
    private Integer wealthVal;
    private Long relationId;
    private Integer type;
}