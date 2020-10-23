package com.rzyou.funtime.entity;


import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeMeipaiAdMonitor implements Serializable {

    private static final long serialVersionUID = -7254254535414746996L;
    private Long id;

    private String os;

    private String imei;

    private String oaid;

    private String idfa;

    private String customerId;

    private String adGroupId;

    private String adId;

    private String creativeId;

    private String ts;

    private String mtLinkSource;

    private String uniid;

    private String callbackUrl;

    private String callback;

    private String ip;

}