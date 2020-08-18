package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FuntimeKuaishouAdMonitor implements Serializable {

    private static final long serialVersionUID = 7081505998909112496L;
    private Long id;

    private String os;

    private Integer aid;

    private Integer cid;

    private Integer did;

    private String dname;

    private String imei2;

    private String imei3;

    private String oaid;

    private String idfa2;

    private String idfa3;

    private String mac;

    private String mac2;

    private String mac3;

    private String androidId2;

    private String androidId3;

    private String ts;

    private String ip;

    private Integer csite;

    private String callback;

    private Date createTime;


}