package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeDeviceInfo;
import com.rzyou.funtime.entity.FuntimeImgeCallback;
import com.rzyou.funtime.entity.FuntimeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.omg.CORBA.OBJ_ADAPTER;
import org.yeauty.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserMapper {

    List<FuntimeUser> getAllQqUser();

    Integer checkUserAllowOffline(Long userId);

    Map<String,Object> getDdzUserInfoById(Long userId);

    List<Long> getBeautyNumbers();

    Integer getAccountState(Long userId);

    Long getMaxShowId();

    Integer checkSensitive(String content);

    Integer checkForbiddenWords(Long userId);

    Integer getBlockDevice(String imei);

    Integer checkDeviceExistsForAndroid(@Param("androidId") String androidId,@Param("point") String point);

    Integer checkDeviceExistsForApple(@Param("idfa") String idfa,@Param("point") String point);

    Map<String,Object> getUserInfoByShowId2(@Param("showId") Long showId,@Param("userId") Long userId);

    List<String> getDefaultNameBySex(Integer sex);

    int updateUserCar(@Param("id") Long id,@Param("carId") Integer carId);

    List<Map<String,Object>> getRankRewardConf(Integer dateType);

    int saveUserInfoChangeLog(@Param("userId") Long userId,@Param("changeColumn") String changeColumn,@Param("changeVal") String changeVal);

    int saveImHeart(@Param("userId") Long userId,@Param("userState") Integer userState
               ,@Param("action") String action,@Param("reason") String reason);

    List<String> getAllUserId();

    List<String> getAllUserIdByApp();

    Map<String, Object> queryUserByChatUser(@Param("userId") Long userId,@Param("byUserId") Long byUserId);

    FuntimeUser queryUserInfo(Map<String,Object> map);

    FuntimeUser queryUserInfoByPhone(String phone);

    FuntimeUser queryUserInfoByImei(String phoneImei);

    List<FuntimeUser> queryUserInfoByOnline(@Param("sex") Integer sex, @Param("startAge") String startAge, @Param("endAge") String endAge, @Param("userId") Long userId);

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

    int updateShowIdById(Long id);

    List<Map<String,Object>> queryAuthorityByRole(Integer userRole);

    Integer checkAuthorityForUserRole(@Param("userRole") Integer userRole,@Param("authority") Integer authority);

    List<FuntimeUser> getConcernUserList(Long userId);

    List<FuntimeUser> getFansList(Long userId);

    List<Map<String, Object>> getContributionList(@Param("startDate") String startDate, @Param("endDate") String endDate,@Param("endCount") int endCount);

    List<Map<String, Object>> getHotList(@Param("startDate") String startDate, @Param("endDate") String endDate,@Param("endCount") int endCount);

    List<Map<String, Object>> getCharmList(@Param("startDate") String startDate, @Param("endDate") String endDate,@Param("endCount") int endCount);

    int saveHeart(@Param("userId") Long userId,@Param("ip") String ip);

    List<Map<String, Object>> getExpression();

    List<Map<String, Object>> getBanners();

    List<FuntimeUser> queryUserInfoByIndex(@Param("content") String content, @Param("userId") Long userId);

    /**
     * 在房间的离线用户
     * @return
     */
    List<Long> getOfflineUser();

    List<Long> getOfflineUserByApp(Integer mins);
    List<Long> getOfflineUserByApp2(Integer mins);

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

    int updateImHeartSync(Long userId);

    Long getUserActivity(Long userId);

    int insertUserActivity(@Param("userId") Long userId,@Param("activityId") Integer activityId);

    int insertFuntimeImgeCallback(FuntimeImgeCallback imgeCallback);

    int insertDeviceInfo(FuntimeDeviceInfo deviceInfo);

    int userCancellation(Long userId);

    int insertUserLocationLog(@Param("userId") Long userId,@Param("longitude") String longitude,@Param("latitude")  String latitude);

    List<Map<String,Object>> getUserList1(@Param("sex") Integer sex, @Param("userId") Long userId);

    List<Map<String,Object>> getInvitationUserList87(@Param("userId") Long userId,@Param("content") String content,@Param("roomId") Long roomId);

    List<Map<String,Object>> getInvitationUserList88(@Param("userId") Long userId,@Param("content") String content,@Param("roomId") Long roomId);

    List<Map<String,Object>> getInvitationUserList89(@Param("userId") Long userId,@Param("content") String content,@Param("roomId") Long roomId);

    List<Map<String,Object>> getUserList2(@Param("sex") Integer sex, @Param("userId") Long userId);

    List<Map<String,Object>> getUserList3(@Param("sex") Integer sex, @Param("userId") Long userId);

    List<Map<String,Object>> getUserList4(@Param("sex") Integer sex, @Param("userId") Long userId, @Param("longitude") BigDecimal longitude,@Param("latitude") BigDecimal latitude);

    List<Map<String,Object>> getInvitationUserList90(@Param("userId") Long userId,@Param("content") String content, @Param("longitude") BigDecimal longitude,@Param("latitude") BigDecimal latitude,@Param("roomId") Long roomId);

    int insertUserAction(@Param("userId") Long userId,@Param("page")  String page,@Param("ip")  String ip);

    int insertUserImDayCount(@Param("userId") Long userId,@Param("counts")  Integer counts);

    Integer getUserImRecord(@Param("userId") Long userId,@Param("toUserId") Long toUserId,@Param("dayTime") Integer dayTime);

    int insertUserImRecord(@Param("userId") Long userId,@Param("toUserId") Long toUserId,@Param("dayTime") Integer dayTime,@Param("unlocked") Integer unlocked);

    Integer getUserImDayCount(@Param("userId") Long userId,@Param("dayTime") Integer dayTime);

    Integer checkBlacklist(@Param("userId") Long userId,@Param("toUserId") Long toUserId);

    int insertUserBlacklist(@Param("userId") Long userId,@Param("toUserId") Long toUserId);

    int delBlacklist(@Param("userId") Long userId,@Param("toUserId") Long toUserId);

    List<Map<String,Object>> getBlacklists(Long userId);

    int updateAccountState(Long id);

    List<Long> checkTokenExists(String token);

    int saveDeviceToken(@Param("token") String token,@Param("userId") Long userId);

    int updateDeviceToken(@Param("token") String token,@Param("userId") Long userId);

    String getUserCounts(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("channel")String channel);

    List<Map<String,Object>> getUserListByDitui(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("channel")String channel);
}