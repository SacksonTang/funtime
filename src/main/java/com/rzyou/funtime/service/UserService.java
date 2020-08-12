package com.rzyou.funtime.service;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserService {

    /**
     * 保存用户来源
     * @param userId
     * @param activityId
     */
    void insertUserActivity( Long userId, Integer activityId);

    /**
     * 查询所有有效用户
     * @return
     */
    List<String> getAllUserId();

    /**
     * 查询所有系统用户
     * @return
     */
    List<String> getAllUserIdByApp();

    /**
     * 检查用户是否存在
     * @param id
     * @return
     */
    boolean checkUserExists(Long id);

    /**
     * 获取用户信息
     * @param id
     * @return
     */
    FuntimeUser queryUserById(Long id);

    /**
     * 获取默认头像
     * @param sex
     * @return
     */
    List<String> getUserImageDefaultUrls(Integer sex);

    /**
     * 获取用户信息
     * @param phone
     * @return
     */
    FuntimeUser queryUserInfoByPhone(String phone);

    /**
     * 修改用户信息
     * @param id
     * @param onlineState
     * @param token
     * @param imei
     * @param ip
     * @param nikename
     * @param loginType
     * @param deviceName
     */
    void updateUserInfo(Long id, Integer onlineState, String token, String imei, String ip, String nikename,String loginType,String deviceName);

    /**
     * 修改用户信息
     * @param user
     */
    void updateUserInfo(FuntimeUser user);

    /**
     * 获取用户基本信息
     * @param id
     * @return
     */
    FuntimeUser getUserBasicInfoById(Long id);

    /**
     * 根据openid查询用户
     * @param openid
     * @param thirdType
     * @return
     */
    FuntimeUserThird queryUserInfoByOpenid(String openid,String thirdType);

    /**
     * 查询用户openid
     * @param userId
     * @param thirdType
     * @return
     */
    String queryUserOpenidByType(Long userId,String thirdType);

    /**
     * 查询用户微信
     * @param userId
     * @param thirdType
     * @return
     */
    FuntimeUserThird queryUserThirdIdByType(Long userId,String thirdType);

    /**
     * 保存用户
     * @param user
     * @param openType
     * @param openid
     * @param unionid
     * @param accessToken
     * @return
     */
    Boolean saveUser(FuntimeUser user, String openType, String openid, String unionid,String accessToken);

    /**
     * 修改用户基本信息
     * @param user
     * @return
     */
    Boolean updateUserBasicInfoById(FuntimeUser user);

    Boolean deleteUser(Long id);

    Boolean enableUser(Long id);


    List<Integer> queryTagsByUserId(Long userId);

    Integer queryTagsByTypeAndName(String tagType, String tagName);

    /**
     * 根据类型获取标签
     * @param tagType
     * @param type
     * @return
     */
    List<Map<String,Object>> queryTagsByType(String tagType, Integer type);

    /**
     * 获取用户账务信息
     * @param userId
     * @return
     */
    FuntimeUserAccount getUserAccountInfoById(Long userId);

    /**
     * 账户增加
     * @param blackDiamond
     * @param blueDiamond
     * @param hornNumber
     */
    void updateUserAccountForPlus(Long userId,BigDecimal blackDiamond,BigDecimal blueDiamond,Integer hornNumber);

    /**
     * 账户增加
     * @param userId
     * @param blackDiamond
     * @param receivedGiftNum
     * @param charmVal
     */
    void updateUserAccountForPlusGift(Long userId, BigDecimal blackDiamond, Integer receivedGiftNum, Integer charmVal);
    /**
     * 账户减少
     * @param blackDiamond
     * @param blueDiamond
     * @param hornNumber
     */
    void updateUserAccountForSub(Long userId,BigDecimal blackDiamond,BigDecimal blueDiamond,Integer hornNumber);

    /**
     * 金币修改（增加）
     * @param userId
     * @param goldCoin
     */
    void updateUserAccountGoldCoinPlus(Long userId, Integer goldCoin);

    /**
     * 金币减少
     * @param userId
     * @param goldCoin
     */
    void updateUserAccountGoldCoinSub(Long userId, Integer goldCoin);
    /**
     * 修改手机号码
     * @param userId
     * @param newPhoneNumber
     * @param code
     * @param oldPhoneNumber
     */
    void updatePhoneNumber(Long userId, String newPhoneNumber, String code, String oldPhoneNumber);

    /**
     * 实名认证
     * @param userId
     * @param fullname
     * @param identityCard
     * @param depositCard
     * @param code
     */
    void saveUserValid(Long userId, String fullname, String identityCard, String depositCard,String code);

    /**
     * 查询实名认证信息
     * @param userId
     * @return
     */
    FuntimeUserValid queryValidInfoByUserId(Long userId);


    /**
     * 关注用户
     * @param userId
     * @param toUserId
     */
    void saveConcern(Long userId,Long toUserId);

    /**
     * 取消关注
     * @param userId
     * @param toUserId
     */
    void deleteConcern(Long userId,Long toUserId);

    /**
     * 修改在线状态
     * @param userId
     * @param onlineState
     */
    void updateOnlineState(Long userId, Integer onlineState);


    /**
     * 修改token
     * @param userId
     */
    void updateShowIdById(Long userId);

    /**
     * 房间数加1
     * @param id
     */
    void updateCreateRoomPlus(Long id);

    /**
     * 房间数减一
     * @param id
     */
    void updateCreateRoomSub(Long id);

    /**
     * 在线用户查询
     * @param startPage
     * @param pageSize
     * @param sex
     * @param ageType
     * @param userId
     * @return
     */
    PageInfo<FuntimeUser> queryUserInfoByOnline(Integer startPage, Integer pageSize, Integer sex, Integer ageType, Long userId);

    /**
     * 用户礼物列表
     * @param userId
     * @return
     */
    List<Map<String,Object>> getGiftByUserId(Long userId);

    /**
     * 查询用户相册
     * @param userId
     * @return
     */
    List<FuntimeUserPhotoAlbum> getPhotoByUserId(Long userId);

    /**
     * 查询聊天室用户
     * @param userId
     * @param byUserId
     * @return
     */
    Map<String,Object> queryUserByChatUser(Long userId, Long byUserId);

    /**
     * 查询角色权限
     * @param userRole
     * @return
     */
    List<Map<String,Object>> queryAuthorityByRole(Integer userRole);

    /**
     * 检验用户角色权限
     * @param userRole
     * @param authority
     * @return
     */
    boolean checkAuthorityForUserRole(Integer userRole,Integer authority);

    /**
     * 关注列表
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    PageInfo<FuntimeUser> getConcernUserList(Integer startPage, Integer pageSize, Long userId);

    /**
     * 粉丝列表
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    PageInfo<FuntimeUser> getFansList(Integer startPage, Integer pageSize, Long userId);

    /**
     * 排行榜
     * @param dateType
     * @param type
     * @param curUserId

     * @return
     */
    Map<String,Object> getRankingList(Integer dateType, Integer type, String curUserId);


    /**
     * 更新相册
     * @param userId
     * @param array
     */
    void updatePhotoByUserId(Long userId, JSONArray array);

    /**
     * 举报
     * @param accusation
     */
    void makeAccusation(FuntimeAccusation accusation);

    /**
     * 保存心跳
     * @param userId
     * @param ipAddr
     */
    void saveHeart(Long userId, String ipAddr);

    /**
     * 保存在线状态变更回调
     * @param userId
     * @param userState
     * @param action
     * @param reason
     */
    void saveImHeart(Long userId,Integer userState,String action,String reason);

    /**
     * 房内离线用户
     * @return
     */
    List<Long> getOfflineUser();

    /**
     * 获取表情
     * @return
     */
    List<Map<String, Object>> getExpression();

    /**
     * 获取banner图
     * @return
     */
    List<Map<String, Object>> getBanners();

    /**
     * 获取首页用户查询
     * @param startPage
     * @param pageSize
     * @param content
     * @param userId
     * @return
     */
    PageInfo<FuntimeUser> queryUserInfoByIndex(Integer startPage, Integer pageSize, String content, Long userId);

    /**
     * 客服
     * @return
     */
    Map<String, Object> getCustomerService();

    /**
     * 获取提现页面数据
     * @param userId
     * @return
     */
    Map<String, Object> getWithdralInfo(Long userId);

    /**
     * 退出登录
     * @param userId
     */
    void logout(Long userId);

    /**
     * 认证手机号
     * @param userId
     * @param code
     * @param oldPhoneNumber
     */
    void validPhone(Long userId, String code, String oldPhoneNumber);

    /**
     * 是否被关注
     * @param userId
     * @param toUserId
     * @return
     */
    boolean checkRecordExist(Long userId,Long toUserId);


    /**
     * 邀请用户列表
     * @param startPage
     * @param pageSize
     * @param userId
     * @param roomId
     * @param type
     * @param content
     * @return
     */
    PageInfo<Map<String,Object>> getInvitationUserList(Integer startPage, Integer pageSize, Long userId, Long roomId, Integer type, String content);

    /**
     * 版本管理
     * @param platform
     * @param appVersion
     * @return
     */
    Map<String,Object> checkVersion(String platform,String appVersion);

    /**
     * 秒验绑定
     * @param userId
     * @param phoneNumber
     */
    void bindPhoneNumber(Long userId, String phoneNumber);

    /**
     * 验证码绑定
     * @param userId
     * @param phoneNumber
     * @param code
     */
    void bindPhoneNumber(Long userId, String phoneNumber, String code);

    /**
     * 根据showId获取用户信息
     * @param showId
     * @return
     */
    FuntimeUser getUserInfoByShowId(String showId);

    /**
     * 根据showId获取用户信息（管理员）
     * @param showId
     * @param userId
     * @return
     */
    Map<String, Object> getUserInfoByShowId2(Long showId, Long userId);

    /**
     * 绑定微信
     * @param userId
     * @param code
     * @param type
     */
    String bindWeixin(Long userId, String code, Integer type);

    /**
     * 绑定QQ
     * @param userId
     * @param accessToken
     * @param type
     */
    String bindQQ(Long userId, String accessToken, Integer type);

    /**
     * 获取设置页信息
     * @param userId
     * @return
     */
    Map<String, Object> getInstallInfo(Long userId);

    /**
     * 心跳定时处理
     */
    void heartTask();

    /**
     * 心跳定时任务
     */
    void offlineUserAppTask();

    /**
     * 修改同步状态
     * @param userId
     */
    void updateImHeartSync(Long userId);

    /**
     * 修改当前坐骑
     * @param userId
     * @param carId
     */
    void updateUserCar(Long userId,Integer carId);

    /**
     * 获取默认名字
     * @param sex
     * @return
     */
    String getDefaultNameBySex(Integer sex);


    /**
     * 用户绑定信息
     * @param userId
     * @return
     */
    Map<String, Object> getUserBindInfo(Long userId);

    /**
     * 实名认证信息
     * @param userId
     * @return
     */
    Map<String, Object> getUserValidInfo(Long userId);

    /**
     * 修改实名认证
     * @param userId
     * @param depositCard
     * @param code
     */
    void updateUserValid(Long userId, String depositCard, String code);

    /**
     * 内容审核
     * @param imgeCallback
     */
    void insertFuntimeImgeCallback(FuntimeImgeCallback imgeCallback);

    /**
     * 埋点
     * @param deviceInfo
     */
    void doPoint(FuntimeDeviceInfo deviceInfo);

    /**
     * 根据设备号查询用户
     * @param phoneImei
     * @return
     */
    FuntimeUser queryUserInfoByPhoneImei(String phoneImei);

    /**
     * 监测敏感词
     * @param content
     */
    void checkSensitive(String content);
}
