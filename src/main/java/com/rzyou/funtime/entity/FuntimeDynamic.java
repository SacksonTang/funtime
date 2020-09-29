package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FuntimeDynamic implements Serializable {

    private static final long serialVersionUID = -3356125447127618996L;
    private Long id;
    
    private Long userId;

    private String dynamic;

    private Integer resourceType;

    private String resource1;

    private String resource2;

    private String resource3;

    private String resource4;

    private String resource5;

    private String resource6;

    private String resource7;

    private String resource8;

    private String resource9;

    private String coverUrl;

    private Date createTime;

}