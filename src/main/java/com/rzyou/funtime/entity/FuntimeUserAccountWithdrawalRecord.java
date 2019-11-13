package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

public class FuntimeUserAccountWithdrawalRecord {
    private Long id;

    private Long userId;

    private Integer withdrawalType;

    private String cardNumber;

    private String thirdOrderNumber;

    private BigDecimal amount;

    private BigDecimal blackDiamond;

    private BigDecimal blueDiamond;

    private BigDecimal blueBlackRatio;

    private BigDecimal blackRmbRatio;

    private BigDecimal channelAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date completeTime;

    private Integer flag;

    private Long version;

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

    public Integer getWithdrawalType() {
        return withdrawalType;
    }

    public void setWithdrawalType(Integer withdrawalType) {
        this.withdrawalType = withdrawalType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber == null ? null : cardNumber.trim();
    }

    public String getThirdOrderNumber() {
        return thirdOrderNumber;
    }

    public void setThirdOrderNumber(String thirdOrderNumber) {
        this.thirdOrderNumber = thirdOrderNumber == null ? null : thirdOrderNumber.trim();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBlackDiamond() {
        return blackDiamond;
    }

    public void setBlackDiamond(BigDecimal blackDiamond) {
        this.blackDiamond = blackDiamond;
    }

    public BigDecimal getBlueDiamond() {
        return blueDiamond;
    }

    public void setBlueDiamond(BigDecimal blueDiamond) {
        this.blueDiamond = blueDiamond;
    }

    public BigDecimal getBlueBlackRatio() {
        return blueBlackRatio;
    }

    public void setBlueBlackRatio(BigDecimal blueBlackRatio) {
        this.blueBlackRatio = blueBlackRatio;
    }

    public BigDecimal getBlackRmbRatio() {
        return blackRmbRatio;
    }

    public void setBlackRmbRatio(BigDecimal blackRmbRatio) {
        this.blackRmbRatio = blackRmbRatio;
    }

    public BigDecimal getChannelAmount() {
        return channelAmount;
    }

    public void setChannelAmount(BigDecimal channelAmount) {
        this.channelAmount = channelAmount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}