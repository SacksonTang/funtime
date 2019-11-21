package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserThird;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserThirdMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserThird record);

    FuntimeUserThird selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserThird record);

    FuntimeUserThird queryUserByOpenid(String openid);

}