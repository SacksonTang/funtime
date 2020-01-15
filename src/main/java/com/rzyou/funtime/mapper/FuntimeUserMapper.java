package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.omg.CORBA.OBJ_ADAPTER;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserMapper {

    List<String> getAllUserId();

    Map<String, Object> queryUserByChatUser(@Param("userId") Long userId,@Param("byUserId") Long byUserId);

    FuntimeUser queryUserInfo(Map<String,Object> map);

    FuntimeUser queryUserInfoByPhone(String phone);

    List<FuntimeUser> queryUserInfoByOnline(@Param("sex") Integer sex,@Param("startAge") String startAge,@Param("endAge") String endAge);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUser record);

    FuntimeUser selectByPrimaryKey(Long id);

    Long checkUserExists(Long id);


    int updateByPrimaryKeySelective(FuntimeUser record);

    int updateOnlineState(@Param("id") Long id, @Param("onlineState") Integer onlineState);

    int updatePhoneNumberById(@Param("id") Long id, @Param("version") Long version, @Param("newVersion") Long newVersion, @Param("phone") String phone);

    int updateConcernsPlus(Long id);
    int updateFansPlus(Long id);
    int updateConcernsSub(Long id);
    int updateFansSub(Long id);

    int updateCreateRoomPlus(Long id);
    int updateCreateRoomSub(Long id);

    /**
     * 修改实名认证状态
     * @param id
     * @return
     */
    int updateRealnameAuthenticationFlagById(Long id);

    int updateTokenById(@Param("id") Long id, @Param("token") String token);

    List<Map<String,Object>> queryAuthorityByRole(Integer userRole);

    Integer checkAuthorityForUserRole(@Param("userRole") Integer userRole,@Param("authority") Integer authority);

    List<Map<String, Object>> getConcernUserList(@Param("userId") Long userId, @Param("onlineState") Integer onlineState);

    List<Map<String, Object>> getFansList(Long userId);

    List<Map<String, Object>> getContributionList(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Map<String, Object>> getCharmList(@Param("startDate") String startDate, @Param("endDate") String endDate);

    int saveHeart(Long userId);

    List<Map<String, Object>> getExpression();

    List<Map<String, Object>> getBanners();

    List<FuntimeUser> queryUserInfoByIndex(String content);

    /**
     * 客服
     * @return
     */
    Map<String, Object> getCustomerService();

    /**
     * 邀请用户列表
     * @param userId
     * @param roomId
     * @return
     */
    List<Map<String, Object>> getInvitationUserList(@Param("userId") Long userId, @Param("roomId") Long roomId);
}