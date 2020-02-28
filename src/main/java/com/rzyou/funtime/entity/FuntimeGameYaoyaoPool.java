package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 2020/2/28
 * LLP-LX
 */
@Data
public class FuntimeGameYaoyaoPool implements Serializable {

    private Integer id;
    private Integer type;
    private Integer quota;
    private Integer initPool;
    private Integer actualPool;
}
