package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeUserAccountHornLog implements Serializable {
    private static final long serialVersionUID = -870589656943863042L;
    private Long id;

    private Long userId;

    private Long relationId;

    private String actionType;

    private String operationType;

    private Integer amount;

    private Date createTime;


}