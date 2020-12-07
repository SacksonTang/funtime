package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeDailyTask implements Serializable {
    private static final long serialVersionUID = -834377441801171876L;

    private Long id;
    private Long userId;
    private Integer taskId;
    private String taskDesc;
    private Integer taskCount;
    private Integer rewardType;
    private Integer reward;
    private Integer completeCount;
    private Integer state;
    private Integer giftId;
    private Integer userValid;
}
