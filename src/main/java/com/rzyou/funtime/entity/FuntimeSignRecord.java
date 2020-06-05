package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeSignRecord implements Serializable {
    private static final long serialVersionUID = -4140197620966682661L;
    private Long id;
    private Long userId;
    private Integer signDate;
}
