package com.rzyou.funtime.entity;

import lombok.Data;

/**
 * 2020/4/7
 * LLP-LX
 */
@Data
public class FuntimeUserAccountFishRecord {
    private Long id;
    private Long userId;
    private Integer type;
    private Integer bullet;
    private Integer bulletPrice;
    private Long roomId;
}
