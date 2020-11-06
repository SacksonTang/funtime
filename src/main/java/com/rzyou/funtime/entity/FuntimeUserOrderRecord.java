package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeUserOrderRecord implements Serializable {

    private static final long serialVersionUID = 2399999807422973043L;
    private Long id;

    private String orderNo;

    private Long userId;

    private Long toUserId;

    private String price;

    private Integer priceAmount;

    private String tagName;

    private String reason;

    private Integer counts;

    private BigDecimal poundage;

    private Integer total;

    private Integer totalRed;

    private String remark;

    private Integer state;

    private Date createTime;

    private Date orderTakingTime;

    private Date completeTime;

    private Date toCompleteTime;

}