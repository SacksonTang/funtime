package com.rzyou.funtime.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

public class FuntimeGift {
    private Integer id;

    private Integer giftTagId;

    private String giftName;

    private BigDecimal originalPrice;

    private BigDecimal activityPrice;

    private String specialEffectTagId;

    private Integer bestowed;

    private String animationType;

    private String animationUrl;

    private BigDecimal animationLength;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    private Integer sort;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGiftTagId() {
        return giftTagId;
    }

    public void setGiftTagId(Integer giftTagId) {
        this.giftTagId = giftTagId;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName == null ? null : giftName.trim();
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getActivityPrice() {
        return activityPrice;
    }

    public void setActivityPrice(BigDecimal activityPrice) {
        this.activityPrice = activityPrice;
    }

    public String getSpecialEffectTagId() {
        return specialEffectTagId;
    }

    public void setSpecialEffectTagId(String specialEffectTagId) {
        this.specialEffectTagId = specialEffectTagId == null ? null : specialEffectTagId.trim();
    }

    public Integer getBestowed() {
        return bestowed;
    }

    public void setBestowed(Integer bestowed) {
        this.bestowed = bestowed;
    }

    public String getAnimationType() {
        return animationType;
    }

    public void setAnimationType(String animationType) {
        this.animationType = animationType == null ? null : animationType.trim();
    }

    public String getAnimationUrl() {
        return animationUrl;
    }

    public void setAnimationUrl(String animationUrl) {
        this.animationUrl = animationUrl == null ? null : animationUrl.trim();
    }

    public BigDecimal getAnimationLength() {
        return animationLength;
    }

    public void setAnimationLength(BigDecimal animationLength) {
        this.animationLength = animationLength;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}