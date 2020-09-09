package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeComment implements Serializable {

    private static final long serialVersionUID = 1346999596039483610L;
    private Long id;

    private Long userId;

    private Long dynamicId;

    private String comment;

    private Long toUserId;

    private Long toCommentId;

    private Date createTime;

}