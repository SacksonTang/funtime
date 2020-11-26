package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Funtime1v1Record implements Serializable {
    private static final long serialVersionUID = 5215531736771449229L;
    private Long id;
    private Long userId;
    private Long roomId;
    private Integer priceType;
    private Integer price;
    private Integer state;
}
