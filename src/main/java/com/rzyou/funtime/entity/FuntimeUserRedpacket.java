package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

public class FuntimeUserRedpacket {
    private Long id;

    private Long userId;

    private Integer redpacketNum;

    private BigDecimal amount;

    private String redpacketDesc;

    private Integer bestowCondition;

    private Integer giftId;

    private Integer state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date invalidTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

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

    public Integer getRedpacketNum() {
        return redpacketNum;
    }

    public void setRedpacketNum(Integer redpacketNum) {
        this.redpacketNum = redpacketNum;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRedpacketDesc() {
        return redpacketDesc;
    }

    public void setRedpacketDesc(String redpacketDesc) {
        this.redpacketDesc = redpacketDesc == null ? null : redpacketDesc.trim();
    }

    public Integer getBestowCondition() {
        return bestowCondition;
    }

    public void setBestowCondition(Integer bestowCondition) {
        this.bestowCondition = bestowCondition;
    }

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Date invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}