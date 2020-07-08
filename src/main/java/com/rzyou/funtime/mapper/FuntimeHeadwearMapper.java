package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountCarRecord;
import com.rzyou.funtime.entity.FuntimeUserAccountHeadwearRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeHeadwearMapper {

    Map<String,Object> getUserInfoById(Long userId);

    Map<String,Object> getHeadwearInfoById(Integer id);

    int insertUserHeadwear(Map<String, Object> map);

    int updateUserHeadwear(Map<String, Object> map);

    Long getUserHeadwearById(@Param("userId") Long userId, @Param("headwearId") Integer headwearId);

    int updateUserHeadwearCurrent(@Param("userId") Long userId, @Param("headwearId") Integer headwearId);

    int insertUserHeadwearCurrent(@Param("userId") Long userId, @Param("headwearId") Integer headwearId,@Param("type") Integer type);

    Integer getCurrnetHeadwear(Long userId);

    int deleteUserHeadwearCurrent(Long userId);

    int updateUserHeadwearCurrent2(Long userId);

    List<Map<String,Object>> getHeadwearInfoForExpire();

    int deleteUserHeadwearById(Long id);

    List<Map<String, Object>> getHeadwearList(Long userId);

    List<Map<String, Object>> getPriceTagByHeadwearNumber(Integer headwearNumber);

    int insertHeadwearRecord(FuntimeUserAccountHeadwearRecord record);
}
