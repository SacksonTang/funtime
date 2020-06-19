package com.rzyou.funtime.entity;


import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeRoomGame21 implements Serializable {
    private static final long serialVersionUID = -6882780144659355208L;
    private Long id;
    private Long roomId;
    private Long userId;
    private Integer micLocation;
    private Integer pokerNum;
    private String pokers;
    private Integer counts;
    private Integer state;
}
