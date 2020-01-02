package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeUserAccount implements Serializable {
    private static final long serialVersionUID = 1770712090611257538L;
    private Long id;

    private Long userId;

    private BigDecimal blackDiamond;

    private String blackDiamondShow;

    private BigDecimal blueDiamond;

    private String blueDiamondShow;

    private Integer hornNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Long version;

    private BigDecimal hornPrice;
}