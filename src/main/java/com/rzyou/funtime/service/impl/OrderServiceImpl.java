package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.entity.FuntimeOrder;
import com.rzyou.funtime.mapper.FuntimeOrderMapper;
import com.rzyou.funtime.service.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    FuntimeOrderMapper orderMapper;

    @Override
    public void addOrder(FuntimeOrder order) {
        if (getOrderById(order.getUserId()) == null) {
            orderMapper.insertOrder(order);
        }else {
            orderMapper.updateOrder(order);
        }
    }

    @Override
    public FuntimeOrder getOrderById(Long userId){
        return orderMapper.getOrderById(userId);
    }

    @Override
    public Map<String, Object> getRecommendationOrderList() {
        Map<String,Object> resultMap = new HashMap<>();
        List<Map<String, Object>> recommendations = orderMapper.getRecommendationOrderList();
        resultMap.put("recommendationList",recommendations);
        return resultMap;
    }

    @Override
    public Map<String, Object> getOrderList(Long lastId, Integer startPage, Integer pageSize, Integer tagId, Integer sex) {
        Map<String,Object> resultMap = new HashMap<>();
        if (startPage == 1){
            lastId = null;
        }
        List<Map<String, Object>> orderList = orderMapper.getOrderList(pageSize, lastId, tagId,sex);

        if (orderList!=null&&!orderList.isEmpty()) {
            resultMap.put("lastId",orderList.get(orderList.size()-1).get("createTime"));
            List<Map<String, Object>> tagList;
            Map<String, Object> tagMap;
            for (Map<String, Object> map : orderList){
                if (map.get("tags")==null){
                    continue;
                }
                String tags = map.get("tags").toString();
                String tagText = map.get("tagText") == null?null:map.get("tagText").toString();
                String[] split = tags.split(",");
                if (split.length>0){
                    tagList = new ArrayList<>();
                    for (String str : split){
                        tagMap = new HashMap<>();
                        String[] tagArray = str.split("/");
                        if(StringUtils.isNotBlank(tagText)&&"其他".equals(tagArray[0])) {
                            tagMap.put("tagName", tagText);
                        }else{
                            tagMap.put("tagName", tagArray[0]);
                        }
                        tagMap.put("tagColor",tagArray[1]);
                        tagList.add(tagMap);

                    }
                    map.remove("tags");
                    map.put("tagNames",tagList);
                }

            }

        }
        resultMap.put("orderList",orderList);

        return resultMap;
    }
}
