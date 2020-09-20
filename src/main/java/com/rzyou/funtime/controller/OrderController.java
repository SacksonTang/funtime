package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeOrder;
import com.rzyou.funtime.service.OrderService;
import com.rzyou.funtime.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * 发布订单
     * @param request
     * @return
     */
    @PostMapping("addOrder")
    public ResultMsg<Object> addOrder(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            FuntimeOrder order = JSONObject.toJavaObject(paramJson,FuntimeOrder.class);
            if (StringUtils.isBlank(order.getOrderTime())){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            order.setUserId(HttpHelper.getUserId());
            orderService.addOrder(order);
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }
    /**
     * 下单详情
     * @param request
     * @return
     */
    @PostMapping("getOrderById")
    public ResultMsg<Object> getOrderById(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            result.setData(orderService.getOrderById(userId));
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 下单列表
     * @param request
     * @return
     */
    @PostMapping("getOrderList")
    public ResultMsg<Object> getOrderList(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize");
            Integer tagId = paramJson.getInteger("tagId");
            Integer sex = paramJson.getInteger("sex");
            startPage = startPage == null?1:startPage;
            pageSize = pageSize == null?20:pageSize;
            Long lastId = paramJson.getLong("lastId");

            Map<String, Object> map = orderService.getOrderList(lastId, startPage, pageSize,tagId,sex);

            result.setData(map);
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 下单推荐列表
     * @param request
     * @return
     */
    @PostMapping("getRecommendationOrderList")
    public ResultMsg<Object> getRecommendationOrderList(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {

            Map<String, Object> map = orderService.getRecommendationOrderList();

            result.setData(map);
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }


}
