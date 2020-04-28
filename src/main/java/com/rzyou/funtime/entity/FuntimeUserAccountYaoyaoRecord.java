package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 2020/3/5
 * LLP-LX
 */
@Data
public class FuntimeUserAccountYaoyaoRecord implements Serializable {

    private static final long serialVersionUID = -1596142179731088574L;
    private Long id;
    private Long userId;
    private Integer type;
    private Integer drawRandom;
    private String drawInfo;
    private Integer drawAmount;
    private Integer basicAmount;
    private Integer userAmont;
    private Integer poolAmount;
    private Integer poolPercent;
    private Integer userExchangeAmount;
}
