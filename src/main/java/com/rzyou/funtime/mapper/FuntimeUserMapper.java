package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.omg.CORBA.OBJ_ADAPTER;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserMapper {

    int saveUserInfoChangeLog(@Param("userId") Long userId,@Param("changeColumn") String changeColumn,@Param("changeVal") String changeVal);

    int saveImHeart(@Param("userId") Long userId,@Param("userState") Integer userState
               ,@Param("action") String action,@Param("reason") String reason);

    List<String> getAllUserId();

    Map<String, Object> queryUserByChatUser(@Param("userId") Long userId,@Param("byUserId") Long byUserId);

    FuntimeUser queryUserInfo(Map<String,Object> map);

    FuntimeUser queryUserInfoByPhone(String phone);

    List<FuntimeUser> queryUserInfoByOnline(@Param("sex") Integer sex,@Param("startAge") String startAge,@Param("endAge") String endAge);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUser record);

    FuntimeUser selectByPrimaryKey(Long id);

    Long checkUserExists(Long id);

    List<String> getUserImageDefaultUrls(Integer sex);

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

    List<Map<String, Object>> getContributionList(@Param("startDate") String startDate, @Param("endDate") String endDate,@Param("count") int count);

    List<Map<String, Object>> getCharmList(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("count") int count);

    int saveHeart(@Param("userId") Long userId,@Param("ip") String ip);

    List<Map<String, Object>> getExpression();

    List<Map<String, Object>> getBanners();

    List<FuntimeUser> queryUserInfoByIndex(String content);

    /**
     * 在房间的离线用户
     * @return
     */
    List<Long> getOfflineUser();

    /**
     * 客服
     * @return
     */
    Map<String, Object> getCustomerService();

    /**
     * 邀请用户列表
     * @param userId
     * @param roomId
     * @param content
     * @return
     */
    List<Map<String, Object>> getInvitationUserList(@Param("userId") Long userId, @Param("roomId") Long roomId,@Param("content") String content);

    List<Map<String, Object>> getInvitationUserList2(@Param("userId") Long userId, @Param("roomId") Long roomId, @Param("content") String content);

    List<Map<String, Object>> getInvitationUserList3(@Param("userId") Long userId, @Param("roomId") Long roomId, @Param("content") String content);

    FuntimeUser getUserInfoByShowId(Long showId);

    void heartTask();
}