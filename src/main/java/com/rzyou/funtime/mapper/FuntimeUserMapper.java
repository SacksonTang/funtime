package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface FuntimeUserMapper {

    FuntimeUser queryUserInfo(Map<String,Object> map);


    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUser record);

    FuntimeUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUser record);

}