package com.rzyou.funtime.service;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserService {

    /**
     * 查询所有有效用户
     * @return
     */
    List<String> getAllUserId();

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

    /**
     * 根据类型获取标签
     * @param tagType
     * @return
     */
    List<Map<String,Object>> queryTagsByType(String tagType);

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
     * 账户减少
     * @param blackDiamond
     * @param blueDiamond
     * @param hornNumber
     */
    void updateUserAccountForSub(Long userId,BigDecimal blackDiamond,BigDecimal blueDiamond,Integer hornNumber);

    /**
     * 修改手机号码
     * @param userId
     * @param newPhoneNumber
     * @param code
     */
    void updatePhoneNumber(Long userId, String newPhoneNumber, String code);

    /**
     * 实名认证
     * @param userId
     * @param fullname
     * @param identityCard
     * @param depositCard
     * @param alipayNo
     * @param wxNo
     */
    void saveUserValid(Long userId, String fullname, String identityCard, String depositCard, String alipayNo, String wxNo);

    /**
     * 查询实名认证信息
     * @param userId
     * @return
     */
    FuntimeUserValid queryValidInfoByUserId(Long userId);

    /**
     * 同意协议
     * @param userId
     * @param agreementType
     */
    void saveUserAgreement(Long userId, Integer agreementType);

    /**
     * 检查是否同意协议
     * @param userId
     * @param withdrawalType
     */
    boolean checkAgreementByuserId(Long userId, Integer withdrawalType);

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
     * @param token
     */
    void updateTokenById(Long userId,String token);

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
     * @return
     */
    PageInfo<FuntimeUser> queryUserInfoByOnline(Integer startPage, Integer pageSize, Integer sex, Integer ageType);

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
    PageInfo<Map<String,Object>> getConcernUserList(Integer startPage, Integer pageSize, Long userId);

    /**
     * 粉丝列表
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    PageInfo<Map<String, Object>> getFansList(Integer startPage, Integer pageSize, Long userId);

    /**
     * 排行榜
     * @param startPage
     * @param pageSize
     * @param dateType
     * @param type
     * @return
     */
    PageInfo<Map<String,Object>> getRankingList(Integer startPage, Integer pageSize, Integer dateType,Integer type);

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
     */
    void saveHeart(Long userId);


}
