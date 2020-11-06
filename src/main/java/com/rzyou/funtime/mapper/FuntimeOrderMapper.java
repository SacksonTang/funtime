package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeOrder;
import com.rzyou.funtime.entity.FuntimeUserOrderRecord;
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

    int insertUserOrderRecord(FuntimeUserOrderRecord record);

    int updateUserOrderRecord(FuntimeUserOrderRecord record);

    FuntimeUserOrderRecord getOrderRecordById(Long id);

    Map<String, Object> getRecordInfoById(Long id);

    List<Map<String, Object>> getReceiveOrders(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("userId") Long userId, @Param("type") Integer type);

    List<Map<String, Object>> getMyOrders(@Param("counts") Integer counts, @Param("lastId") Long lastId, @Param("userId") Long userId, @Param("type") Integer type);
}
