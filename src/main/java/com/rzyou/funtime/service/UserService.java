package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeTag;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;

import java.util.List;

public interface UserService {

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
}
