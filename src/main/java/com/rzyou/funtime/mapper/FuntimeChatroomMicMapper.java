package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroomMic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeChatroomMicMapper {
    int deleteByPrimaryKey(Long id);

    int insertBatch(@Param("mics") List<FuntimeChatroomMic> mics);

    int insertSelective(FuntimeChatroomMic record);

    FuntimeChatroomMic selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeChatroomMic record);

    int updateByPrimaryKey(FuntimeChatroomMic record);
}