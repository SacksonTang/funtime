package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.*;
import com.rzyou.funtime.entity.FuntimeOrder;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.entity.FuntimeUserOrderRecord;
import com.rzyou.funtime.mapper.FuntimeOrderMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.OrderService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import com.rzyou.funtime.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    ParameterService parameterService;
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
    public Map<String, Object> getOrderList(Long lastId, Integer startPage, Integer pageSize, Integer tagId) {
        Map<String,Object> resultMap = new HashMap<>();
        if (startPage == 1){
            lastId = null;
        }
        tagId = tagId == 0 ?null:tagId;
        List<Map<String, Object>> orderList = orderMapper.getOrderListForPc(pageSize, lastId, tagId);

        if (orderList!=null&&!orderList.isEmpty()) {
            resultMap.put("lastId",orderList.get(orderList.size()-1).get("createTime"));
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
                        tagId = key;
                        map.put("price", priceMap.get(key).get("price"));
                        break;
                    }

                }

                map.put("tagId",tagId);
                String tags = map.get("tags").toString();
                String tagText = map.get("tagText") == null?null:map.get("tagText").toString();
                String game = map.get("game") == null?null:map.get("game").toString();
                String[] split = tags.split(",");
                if (split.length>0){
                    for (String str : split){
                        String[] tagArray = str.split("/");
                        if (tagArray[1].equals(tagId.toString())){
                            if(StringUtils.isNotBlank(tagText)&&"其他".equals(tagArray[0])) {
                                map.put("tagName", tagText);
                            }else if (StringUtils.isNotBlank(game)&&"开黑".equals(tagArray[0])){
                                map.put("tagName", game);
                            }
                            else{
                                map.put("tagName", tagArray[0]);
                            }
                            break;
                        }
                    }
                }
                map.remove("tagText");
                map.remove("game");
                map.remove("tags");

            }

        }
        resultMap.put("orderList",orderList);

        return resultMap;
    }

    @Override
    public Map<String, Object> getReceiveOrders(Integer type, Long userId, Long lastId, Integer startPage, Integer pageSize) {
        Map<String,Object> resultMap = new HashMap<>();
        if (startPage == 1){
            lastId = null;
        }
        List<Map<String, Object>> orderList = orderMapper.getReceiveOrders(pageSize, lastId, userId,type);
        if (orderList!=null&&!orderList.isEmpty()) {
            resultMap.put("lastId", orderList.get(orderList.size() - 1).get("id"));
            resultMap.put("orderList",orderList);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getMyOrders(Integer type, Long userId, Long lastId, Integer startPage, Integer pageSize) {
        Map<String,Object> resultMap = new HashMap<>();
        if (startPage == 1){
            lastId = null;
        }
        List<Map<String, Object>> orderList = orderMapper.getMyOrders(pageSize, lastId, userId,type);
        if (orderList!=null&&!orderList.isEmpty()) {
            resultMap.put("lastId", orderList.get(orderList.size() - 1).get("id"));
            resultMap.put("orderList",orderList);
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> createOrder(Long userId, Long toUserId, Integer tagId, Integer counts, String remark, String tagName) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
        if (userAccount==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        FuntimeOrder order = orderMapper.getOrderById(toUserId);
        if (order == null){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_STOP.getValue(),ErrorMsgEnum.USER_ORDER_STOP.getDesc());
        }

        FuntimeUserOrderRecord userOrderRecord = new FuntimeUserOrderRecord();
        Map<Integer, Map<String, Object>> priceMap ;
        try {
            priceMap = doPrice(order.getPrice());
            Map<String, Object> tagPriceMap = priceMap.get(tagId);
            Integer priceAmount = Integer.parseInt(tagPriceMap.get("priceAmount").toString());
            //账户余额不足
            if (userAccount.getBlueDiamond().intValue()<priceAmount*counts){
                resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
                resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
                resultMsg.setData(JsonUtil.getMap("amount",priceAmount*counts));
                return resultMsg;
            }
            String order_poundage = parameterService.getParameterValueByKey("order_poundage");
            BigDecimal poundagePer = order_poundage == null?new BigDecimal(0.05):new BigDecimal(order_poundage);
            userOrderRecord.setPriceAmount(priceAmount);
            userOrderRecord.setPrice(tagPriceMap.get("price").toString());
            userOrderRecord.setPoundage(poundagePer.multiply(new BigDecimal(priceAmount*counts)).setScale(2,BigDecimal.ROUND_HALF_UP));
            userOrderRecord.setTotal(priceAmount*counts);
            userOrderRecord.setTotalRed(priceAmount*counts);

        } catch (Exception e) {
            throw new BusinessException(ErrorMsgEnum.COMMENT_PRICE_ERROR.getValue(),ErrorMsgEnum.COMMENT_PRICE_ERROR.getDesc());
        }

        userOrderRecord.setCounts(counts);
        userOrderRecord.setTagName(tagName);
        userOrderRecord.setOrderNo("O"+ StringUtil.createOrderId());
        userOrderRecord.setRemark(remark);
        userOrderRecord.setState(OrderState.START.getValue());
        userOrderRecord.setUserId(userId);
        userOrderRecord.setToUserId(toUserId);

        int k = orderMapper.insertUserOrderRecord(userOrderRecord);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        userService.updateUserAccountForSub(userId,null,new BigDecimal(userOrderRecord.getTotal()),null);
        accountService.saveUserAccountBlueLog(userId,new BigDecimal(userOrderRecord.getTotal()),userOrderRecord.getId(), OperationType.ORDER_OUT.getAction(),OperationType.ORDER_OUT.getOperationType(),null);
        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void cancelOrder(Long userId, Long orderId) {
        FuntimeUserOrderRecord record = orderMapper.getOrderRecordById(orderId);
        if (record == null){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_EXIST.getValue(),ErrorMsgEnum.USER_ORDER_NOT_EXIST.getDesc());
        }
        if (!record.getUserId().equals(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_MINE.getValue(),ErrorMsgEnum.USER_ORDER_NOT_MINE.getDesc());
        }
        if (record.getState()!=1){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_STATE_ERROR.getValue(),ErrorMsgEnum.USER_ORDER_STATE_ERROR.getDesc());
        }
        updateUserOrderRecord(orderId,OrderState.CANCEL.getValue(),null,new Date(),null, null);
        userService.updateUserAccountForPlus(userId,null,new BigDecimal(record.getTotal()),null);
        accountService.saveUserAccountBlueLog(userId,new BigDecimal(record.getTotal()),record.getId(), OperationType.ORDER_CANCEL.getAction(),OperationType.ORDER_CANCEL.getOperationType(),null);
    }

    @Override
    public void receiveOrder(Long userId, Long orderId) {
        FuntimeUserOrderRecord record = orderMapper.getOrderRecordById(orderId);
        if (record == null){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_EXIST.getValue(),ErrorMsgEnum.USER_ORDER_NOT_EXIST.getDesc());
        }
        if (!record.getToUserId().equals(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_MINE.getValue(),ErrorMsgEnum.USER_ORDER_NOT_MINE.getDesc());
        }
        if (record.getState()!=1){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_STATE_ERROR.getValue(),ErrorMsgEnum.USER_ORDER_STATE_ERROR.getDesc());
        }
        updateUserOrderRecord(orderId,OrderState.RECEIVED.getValue(),new Date(),null,null, null);
    }

    @Override
    public void refuseOrder(Long userId, Long orderId, String rejectionReason) {
        FuntimeUserOrderRecord record = orderMapper.getOrderRecordById(orderId);
        if (record == null){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_EXIST.getValue(),ErrorMsgEnum.USER_ORDER_NOT_EXIST.getDesc());
        }
        if (!record.getToUserId().equals(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_MINE.getValue(),ErrorMsgEnum.USER_ORDER_NOT_MINE.getDesc());
        }
        if (record.getState()!=1){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_STATE_ERROR.getValue(),ErrorMsgEnum.USER_ORDER_STATE_ERROR.getDesc());
        }
        updateUserOrderRecord(orderId,OrderState.REFUSED.getValue(),null,new Date(),null,rejectionReason);
        userService.updateUserAccountForPlus(userId,null,new BigDecimal(record.getTotal()),null);
        accountService.saveUserAccountBlueLog(userId,new BigDecimal(record.getTotal()),record.getId(), OperationType.ORDER_REFUSE.getAction(),OperationType.ORDER_REFUSE.getOperationType(),null);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void completeOrder(Long userId, Long orderId) {
        FuntimeUserOrderRecord record = orderMapper.getOrderRecordById(orderId);
        if (record == null){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_EXIST.getValue(),ErrorMsgEnum.USER_ORDER_NOT_EXIST.getDesc());
        }

        if (record.getToUserId().equals(userId)){
            if (!record.getState().equals(OrderState.RECEIVED.getValue())){
                throw new BusinessException(ErrorMsgEnum.USER_ORDER_STATE_ERROR.getValue(),ErrorMsgEnum.USER_ORDER_STATE_ERROR.getDesc());
            }
            //本人是接单人
            updateUserOrderRecord(orderId,OrderState.SERVICEOVER.getValue(),null,null,new Date(), null);
        }else if (record.getUserId().equals(userId)){
            if (!record.getState().equals(OrderState.RECEIVED.getValue())&&!record.getState().equals(OrderState.SERVICEOVER.getValue())){
                throw new BusinessException(ErrorMsgEnum.USER_ORDER_STATE_ERROR.getValue(),ErrorMsgEnum.USER_ORDER_STATE_ERROR.getDesc());
            }
            //本人是消费者
            updateUserOrderRecord(orderId,OrderState.COMPLETE.getValue(),null,new Date(),null, null);
            //加红钻
            userService.updateUserAccountForPlus(record.getToUserId(),new BigDecimal(record.getTotalRed()).subtract(record.getPoundage()),null,null);
            accountService.saveUserAccountBlackLog(record.getToUserId(),new BigDecimal(record.getTotal()),record.getId(), OperationType.ORDER_IN.getAction(),OperationType.ORDER_IN.getOperationType());

        }else{
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_ERROR.getValue(),ErrorMsgEnum.USER_ORDER_ERROR.getDesc());
        }
    }

    @Override
    public Map<String, Object> getRecordInfoById(Long id) {
        return orderMapper.getRecordInfoById(id);
    }

    @Override
    public void refundOrder(Long userId, Long orderId, String reason) {
        FuntimeUserOrderRecord record = orderMapper.getOrderRecordById(orderId);
        if (record == null){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_EXIST.getValue(),ErrorMsgEnum.USER_ORDER_NOT_EXIST.getDesc());
        }
        if (!record.getUserId().equals(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_NOT_MINE.getValue(),ErrorMsgEnum.USER_ORDER_NOT_MINE.getDesc());
        }
        if (!record.getState().equals(OrderState.RECEIVED.getValue())&&!record.getState().equals(OrderState.SERVICEOVER.getValue())){
            throw new BusinessException(ErrorMsgEnum.USER_ORDER_STATE_ERROR.getValue(),ErrorMsgEnum.USER_ORDER_STATE_ERROR.getDesc());
        }
        updateUserOrderRecord(orderId,OrderState.REFUND.getValue(),null,null,null, reason);

    }

    public void updateUserOrderRecord(Long id, Integer state, Date orderTakingTime, Date completeTime, Date toCompleteTime, String rejectionReason){
        FuntimeUserOrderRecord record = new FuntimeUserOrderRecord();
        record.setId(id);
        record.setState(state);
        record.setOrderTakingTime(orderTakingTime);
        record.setCompleteTime(completeTime);
        record.setToCompleteTime(toCompleteTime);
        record.setReason(rejectionReason);
        int k = orderMapper.updateUserOrderRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
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
