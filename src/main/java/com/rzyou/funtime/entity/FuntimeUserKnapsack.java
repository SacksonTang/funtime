package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeUserKnapsack implements Serializable {
    private static final long serialVersionUID = 1406297352161699522L;
    private Long id;
    private Long userId;
    private Integer type;
    private Integer itemId;
    private Integer itemNum;
    private Long version;
}
