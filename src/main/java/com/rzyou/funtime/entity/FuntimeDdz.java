package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeDdz implements Serializable {
    private static final long serialVersionUID = -200674513456638546L;
    private Long id;
    private Long user1;
    private Integer gold1;
    private Long user2;
    private Integer gold2;
    private Long user3;
    private Integer gold3;
}
