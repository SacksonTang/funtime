package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.omg.CORBA.OBJ_ADAPTER;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserMapper {

    Map<String, Object> queryUserByChatUser(@Param("userId") Long userId,@Param("byUserId") Long byUserId);

    FuntimeUser queryUserInfo(Map<String,Object> map);

    FuntimeUser queryUserInfoByPhone(String phone);

    List<FuntimeUser> queryUserInfoByOnline(@Param("sex") Integer sex,@Param("startAge") String startAge,@Param("endAge") String endAge);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUser record);

    FuntimeUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUser record);

    int updateOnlineState(@Param("id") Long id, @Param("onlineState") Integer onlineState);

    int updatePhoneNumberById(@Param("id") Long id, @Param("version") Long version, @Param("newVersion") Long newVersion, @Param("phone") String phone);

    int updateConcernsPlus(Long id);
    int updateFansPlus(Long id);
    int updateConcernsSub(Long id);
    int updateFansSub(Long id);

    int updateCreateRoomPlus(Long id);
    int updateCreateRoomSub(Long id);

    int updateTokenById(@Param("id") Long id, @Param("token") String token);
}