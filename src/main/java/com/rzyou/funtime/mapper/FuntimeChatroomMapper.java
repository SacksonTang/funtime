package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroom;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeChatroomMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeChatroom record);

    FuntimeChatroom selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeChatroom record);

    int updateByPrimaryKey(FuntimeChatroom record);

    int updateOnlineNumPlus(Long id);

    int updateOnlineNumSub(Long id);
}