package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class FuntimeUserAccountRechargeRecord implements Serializable {
    private static final long serialVersionUID = 4544338349394994130L;
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private BigDecimal rmb;

    private String rechargeCardId;

    private Integer rechargeChannelId;

    private Integer rechargeConfId;

    private Integer hornNum;

    private String os;

    private String phoneType;

    private String imei;

    private Integer state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Long version;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date completeTime;

    private String orderNo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRmb() {
        return rmb;
    }

    public void setRmb(BigDecimal rmb) {
        this.rmb = rmb;
    }

    public String getRechargeCardId() {
        return rechargeCardId;
    }

    public void setRechargeCardId(String rechargeCardId) {
        this.rechargeCardId = rechargeCardId == null ? null : rechargeCardId.trim();
    }

    public Integer getRechargeChannelId() {
        return rechargeChannelId;
    }

    public void setRechargeChannelId(Integer rechargeChannelId) {
        this.rechargeChannelId = rechargeChannelId;
    }

    public Integer getRechargeConfId() {
        return rechargeConfId;
    }

    public void setRechargeConfId(Integer rechargeConfId) {
        this.rechargeConfId = rechargeConfId;
    }

    public Integer getHornNum() {
        return hornNum;
    }

    public void setHornNum(Integer hornNum) {
        this.hornNum = hornNum;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os == null ? null : os.trim();
    }

    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType == null ? null : phoneType.trim();
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei == null ? null : imei.trim();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }
}