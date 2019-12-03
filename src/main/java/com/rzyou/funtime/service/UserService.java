package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserService {

    FuntimeUser queryUserById(Long id);

    FuntimeUser queryUserInfoByPhone(String phone);

    void updateUserInfo(Long id, Integer onlineState, String token, String imei, String ip, String nikename,String loginType,String deviceName);

    void updateUserInfo(FuntimeUser user);

    FuntimeUser getUserBasicInfoById(Long id);

    FuntimeUserThird queryUserInfoByOpenid(String openid,String thirdType);

    Boolean saveUser(FuntimeUser user, String openType, String openid, String unionid);

    Boolean updateUserBasicInfoById(FuntimeUser user);

    Boolean deleteUser(Long id);

    Boolean enableUser(Long id);


    List<Integer> queryTagsByUserId(Long userId);

    List<FuntimeTag> queryTagsByType(String tagType);

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

    void updatePhoneNumber(Long userId, String newPhoneNumber, String code);

    void saveUserValid(Long userId, String fullname, String identityCard, String depositCard, String alipayNo, String wxNo);

    FuntimeUserValid queryValidInfoByUserId(Long userId);

    void saveUserAgreement(Long userId, Integer agreementType);

    void checkAgreementByuserId(Long userId, Integer withdrawalType);

    void saveConcern(Long userId,Long toUserId);

    void deleteConcern(Long userId,Long toUserId);

    void updateOnlineState(Long userId, Integer onlineState);

    void updateTokenById(Long userId,String token);

    void updateCreateRoomPlus(Long id);

    void updateCreateRoomSub(Long id);

    PageInfo<FuntimeUser> queryUserInfoByOnline(Integer startPage, Integer pageSize, Integer sex, Integer ageType);

    List<Map<String,Object>> getGiftByUserId(Long userId);

    List<FuntimeUserPhotoAlbum> getPhotoByUserId(Long userId);

    Map<String,Object> queryUserByChatUser(Long userId, Long byUserId);

    List<Integer> queryAuthorityByRole(Integer userRole);
}
