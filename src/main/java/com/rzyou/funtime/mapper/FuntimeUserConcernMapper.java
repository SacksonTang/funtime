package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserConcern;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeUserConcernMapper {

    Long checkRecordExist(@Param("userId") Long userId,@Param("toUserId") Long toUserId);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserConcern record);

    FuntimeUserConcern selectByPrimaryKey(Long id);


    Integer checkFriendExist(@Param("userId") Long userId,@Param("toUserId") Long toUserId);

    int insertUserFriend(@Param("userId") Long userId,@Param("toUserId") Long toUserId);

    int delUserFriend(@Param("userId") Long userId,@Param("toUserId") Long toUserId);


}