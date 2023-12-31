package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserThird;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeUserThirdMapper {
    int deleteByPrimaryKey(Long id);

    int deleteByUserId(Long userId);

    int insertSelective(FuntimeUserThird record);

    FuntimeUserThird selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserThird record);

    FuntimeUserThird queryUserByOpenid(@Param("openid") String openid,@Param("thirdType") String thirdType);

    FuntimeUserThird queryUserThirdIdByType(@Param("userId") Long userId,@Param("thirdType") String thirdType);

    String queryUserOpenidByType(@Param("userId") Long userId,@Param("thirdType") String thirdType);
}