package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroomUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeChatroomUserMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeChatroomUser record);

    FuntimeChatroomUser selectByPrimaryKey(Long id);

    Long checkUserIsExist(@Param("roomId") Long roomId, @Param("userId") Long userId);

}