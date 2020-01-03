package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class FuntimeWithdrawalConf implements Serializable {
    private static final long serialVersionUID = 5340681771704808017L;
    private Integer id;

    private Integer fromAmount;

    private Integer toAmount;

    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;


}