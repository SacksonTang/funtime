package com.rzyou.funtime.entity.music;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeMusicTag implements Serializable {
    private static final long serialVersionUID = 7413024859253087079L;
    private Long id;
    private Long userId;
    private String tagName;
}
