package com.rzyou.funtime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeMusicMapper {

    List<Map<String,Object>> getLocalMusics(@Param("startPage") Integer startPage, @Param("pageSize") Integer pageSize, @Param("tagId") Integer tagId,@Param("content")  String content);
}