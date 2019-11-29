package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroomKickedRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeChatroomKickedRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeChatroomKickedRecord record);

    FuntimeChatroomKickedRecord selectByPrimaryKey(Long id);

    Integer checkUserIsKickedOrNot(@Param("roomId") Long roomId, @Param("kickedUserId") Long kickedUserId);


}