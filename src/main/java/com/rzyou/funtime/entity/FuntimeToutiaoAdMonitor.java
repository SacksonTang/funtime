package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeToutiaoAdMonitor implements Serializable {

    private static final long serialVersionUID = -1803265366926582196L;
    private Long id;

    private String cid;

    private Integer os;

    private String imeimd5;

    private String idfa;

    private String ts;

    private String tsms;

    private String callbackUrl;

    private String unit;

    private String plan;

    private String uid;

    private String ua;

    private String androididmd5;

    private String ip;

    private String oaid;

    private String dpLink;

    private Date createTime;

    
}