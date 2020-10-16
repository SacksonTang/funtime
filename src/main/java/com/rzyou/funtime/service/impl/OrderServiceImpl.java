package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
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
        if (StringUtils.isBlank(order.getPrice())){
            throw new BusinessException(ErrorMsgEnum.COMMENT_PRICE_ERROR.getValue(),ErrorMsgEnum.COMMENT_PRICE_ERROR.getDesc());
        }
        try {
            doPrice(order.getPrice());
        } catch (Exception e) {
            throw new BusinessException(ErrorMsgEnum.COMMENT_PRICE_ERROR.getValue(),ErrorMsgEnum.COMMENT_PRICE_ERROR.getDesc());
        }
        if (orderMapper.checkOrder(order.getUserId()) == null) {
            orderMapper.insertOrder(order);
        }else {
            orderMapper.updateOrder(order);
        }
    }

    @Override
    public Map<String,Object> getOrderById(Long userId){
        Map<String,Object> resultMap = new HashMap<>();
        FuntimeOrder order = orderMapper.getOrderById(userId);
        if (order!=null){
            String orderTime = order.getOrderTime();
            String startHour = order.getStartHour();
            String endHour = order.getEndHour();
            orderTime = orderTime.replace("1","周一")
                    .replace("2","周二")
                    .replace("3","周三")
                    .replace("4","周四")
                    .replace("5","周五")
                    .replace("6","周六")
                    .replace("7","周日")
                    .replaceAll(",","、")+" "+startHour+"~"+endHour
            ;
            String serviceTag = order.getServiceTag();
            String price = order.getPrice();
            if (StringUtils.isNotBlank(price)&&StringUtils.isNotBlank(serviceTag)) {

                Map<Integer, Map<String, Object>> tagMap;
                try {
                    tagMap = doPrice(price);
                } catch (Exception e) {
                    throw new BusinessException(ErrorMsgEnum.COMMENT_PRICE_ERROR.getValue(),ErrorMsgEnum.COMMENT_PRICE_ERROR.getDesc());
                }

                List<Map<String, Object>> serviceTags = orderMapper.getServiceTags(serviceTag);
                if (serviceTags != null && !serviceTags.isEmpty()) {
                    for (Map<String, Object> serviceMap : serviceTags) {
                        Integer tagId = Integer.parseInt(serviceMap.get("id").toString());
                        Map<String, Object> priceMap = tagMap.get(tagId);
                        serviceMap.put("priceName",priceMap.get("priceName"));
                        serviceMap.put("priceAmount",priceMap.get("priceAmount"));
                        if (StringUtils.isNotBlank(order.getTagText())&&"其他".equals(serviceMap.get("tagName").toString())){
                            serviceMap.put("tagName",order.getTagText());
                        }
                        if (StringUtils.isNotBlank(order.getGame())&&"开黑".equals(serviceMap.get("tagName").toString())){
                            serviceMap.put("tagName",order.getGame());
                        }
                    }
                    resultMap.put("tags",serviceTags);
                }
            }

            resultMap.put("orderTime",orderTime);
            resultMap.put("serviceText",order.getServiceText());
        }

        return resultMap;
    }

    public Map<Integer, Map<String, Object>> doPrice(String price) throws Exception{
        Map<Integer, Map<String, Object>> tagMap = new HashMap<>();

        String[] split = price.split(",");
        Map<String, Object> map;
        for (String sp : split) {
            map = new HashMap<>();
            String[] spArray = sp.split("/");
            Integer tagId = Integer.parseInt(spArray[0]);
            Integer priceType = Integer.parseInt(spArray[1]);
            Integer priceAmount = Integer.parseInt(spArray[2]);
            map.put("tagId", tagId);
            map.put("priceName", priceType == 1 ? "蓝钻/次" : "蓝钻/10分钟");
            map.put("priceAmount", priceAmount);
            map.put("price", priceAmount+(priceType == 1 ? "蓝钻/次" : "蓝钻/10分钟"));

            tagMap.put(tagId, map);
        }
        return tagMap;
    }

    @Override
    public Map<String, Object> getRecommendationOrderList(Integer tagId) {
        Map<String,Object> resultMap = new HashMap<>();
        tagId = tagId == 0 ?null:tagId;
        List<Map<String, Object>> recommendations = orderMapper.getRecommendationOrderList(tagId);
        Map<Integer, Map<String, Object>> priceMap;
        Map<String, Object> tagPriceMap;
        for (Map<String, Object> map : recommendations){
            String price = map.get("price").toString();
            try {
                priceMap = doPrice(price);
            } catch (Exception e) {
                throw new BusinessException(ErrorMsgEnum.COMMENT_PRICE_ERROR.getValue(),ErrorMsgEnum.COMMENT_PRICE_ERROR.getDesc());
            }
            if (tagId!=null) {
                tagPriceMap = priceMap.get(tagId);
                if (tagPriceMap != null) {
                    map.put("price", tagPriceMap.get("price"));
                }
            }else{
                for (Integer key : priceMap.keySet()){
                    map.put("price", priceMap.get(key).get("price"));
                    break;
                }

            }
        }
        resultMap.put("recommendationList",recommendations);
        return resultMap;
    }

    @Override
    public Map<String, Object> getOrderList(Long lastId, Integer startPage, Integer pageSize, Integer tagId, Integer sex) {
        Map<String,Object> resultMap = new HashMap<>();
        if (startPage == 1){
            lastId = null;
        }
        tagId = tagId == 0 ?null:tagId;
        List<Map<String, Object>> orderList = orderMapper.getOrderList(pageSize, lastId, tagId,sex);

        if (orderList!=null&&!orderList.isEmpty()) {
            resultMap.put("lastId",orderList.get(orderList.size()-1).get("createTime"));
            List<Map<String, Object>> tagList;
            Map<String, Object> tagMap;
            for (Map<String, Object> map : orderList){
                if (map.get("tags")==null){
                    continue;
                }
                String price = map.get("price").toString();
                Map<Integer, Map<String, Object>> priceMap = null;
                try {
                    priceMap = doPrice(price);
                } catch (Exception e) {
                    throw new BusinessException(ErrorMsgEnum.COMMENT_PRICE_ERROR.getValue(),ErrorMsgEnum.COMMENT_PRICE_ERROR.getDesc());
                }
                if (tagId!=null) {
                    Map<String, Object> tagPriceMap = priceMap.get(tagId);
                    if (tagPriceMap != null) {
                        map.put("price", tagPriceMap.get("price"));
                    }
                }else{
                    for (Integer key : priceMap.keySet()){
                        map.put("price", priceMap.get(key).get("price"));
                        break;
                    }

                }

                String tags = map.get("tags").toString();
                String tagText = map.get("tagText") == null?null:map.get("tagText").toString();
                String game = map.get("game") == null?null:map.get("game").toString();
                String[] split = tags.split(",");
                if (split.length>0){
                    tagList = new ArrayList<>();
                    for (String str : split){
                        tagMap = new HashMap<>();
                        String[] tagArray = str.split("/");
                        if(StringUtils.isNotBlank(tagText)&&"其他".equals(tagArray[0])) {
                            tagMap.put("tagName", tagText);
                        }else if (StringUtils.isNotBlank(game)&&"开黑".equals(tagArray[0])){
                            tagMap.put("tagName", game);
                        }
                        else{
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
