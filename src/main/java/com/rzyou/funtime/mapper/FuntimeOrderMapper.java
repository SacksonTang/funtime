package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeOrderMapper {

    int insertOrder(FuntimeOrder order);

    int updateOrder(FuntimeOrder order);

    FuntimeOrder getOrderById(Long userId);

    Integer checkOrder(Long userId);

    List<Map<String,Object>> getServiceTags(String serviceTag);

    List<Map<String,Object>> getOrderList(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("tagId") Integer tagId, @Param("sex") Integer sex);

    List<Map<String,Object>> getOrderListForPc(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("tagId") Integer tagId);

    List<Map<String, Object>> getRecommendationOrderList(Integer tagId);
}
