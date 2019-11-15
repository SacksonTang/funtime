package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeTag;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {

    FuntimeUser queryUserById(Long id);

    FuntimeUser queryUserInfoByPhone(String phone);

    void updateUserInfo(Long id, Integer onlineState, String token, String imei, String ip, Long version,String nikename,String loginType,String deviceName);

    FuntimeUser getUserBasicInfoById(Long id);

    Boolean saveUser(FuntimeUser user);

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
}
