package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeUserValid implements Serializable {
    private static final long serialVersionUID = -3026977537182263576L;
    private Long id;

    private Long userId;

    private String fullname;

    private String identityCard;

    private String depositCard;

    private String alipayNo;

    private String wxNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private String depositCardReal;
}