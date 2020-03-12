package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.entity.FuntimeUserAccountRedpacketRecord;
import com.rzyou.funtime.entity.FuntimeUserRedpacket;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/redpacket")
public class RedpacketController {

    @Autowired
    AccountService accountService;
    @Autowired
    ParameterService parameterService;

    /**
     * 房间红包已抢记录
     */
    @PostMapping("getRecordListByRedId")
    public ResultMsg<Object> getRecordListByRedId(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long redpacketId = paramJson.getLong("redpacketId");
            Long userId = paramJson.getLong("userId");
            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("records",accountService.getRecordListByRedId(redpacketId));
            resultMap.put("redpacket",accountService.getRedpacketInfoById(redpacketId,userId));
            result.setData(resultMap);
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
     * 房间待抢红包列表
     * @param request
     * @return
     */
    @PostMapping("getRedpacketListByRoomId")
    public ResultMsg<Object> getRedpacketListByRoomId(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?10:paramJson.getInteger("pageSize");
            Long roomId = paramJson.getLong("roomId");
            Long userId = paramJson.getLong("userId");
            result.setData(JsonUtil.getMap("pageInfo",accountService.getRedpacketListByRoomId(startPage,pageSize,roomId,userId)));
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
     * 创建红包
     */
    @PostMapping("createRedpacket")
    public ResultMsg<Object> createRedpacket(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson;
            String flag = parameterService.getParameterValueByKey("is_encrypt");
            if (flag!=null&&flag.equals("1")){
                paramJson = HttpHelper.getParamterJsonDecrypt(request);
            }else{
                paramJson = HttpHelper.getParamterJson(request);
            }

            FuntimeUserRedpacket redpacket = JSONObject.toJavaObject(paramJson, FuntimeUserRedpacket.class);
            if (redpacket==null||redpacket.getAmount().intValue()<=0||(redpacket.getType() == 1&&redpacket.getRedpacketNum()<5)
                ||(redpacket.getType()==2&&redpacket.getToUserId()==null)) {

                    result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                    result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                    return result;
            }

            Long id = accountService.createRedpacket(redpacket);

            result.setData(JsonUtil.getMap("id",id));
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
     * 抢红包
     */
    @PostMapping("grabRedpacket")
    public ResultMsg<Object> grabRedpacket(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Long userId = paramJson.getLong("userId");
            Long redpacketId = paramJson.getLong("redpacketId");

            if (userId==null||redpacketId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            return accountService.grabRedpacket(userId,redpacketId);
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
     * 获取红包明细
     */
    @PostMapping("getRedpacketForPage")
    public ResultMsg<Object> getRedpacketForPage(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?10:paramJson.getInteger("pageSize");
            String queryDate = paramJson.getString("queryDate");
            Integer type = paramJson.getInteger("type");
            Long userId = paramJson.getLong("userId");
            if(StringUtils.isBlank(queryDate)||type==null||userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> resultMap = new HashMap<>();
            //发出
            if(type.intValue()==1){
                PageInfo<FuntimeUserRedpacket> pageInfo = accountService.getRedpacketOfSendForPage(startPage, pageSize, queryDate, userId);
                resultMap.put("pageInfo",pageInfo);
                resultMap.put("sendAmountTotal",accountService.querySnedSumAmountByGrab(userId,queryDate));
                resultMap.put("sendNumTotal",pageInfo.getTotal());

            }else{
                resultMap.put("pageInfo",accountService.getRedpacketOfRecieveForPage(startPage, pageSize, queryDate, userId));
                resultMap.put("grabAmountTotal",accountService.getSumGrabAmountById(userId,queryDate));
                resultMap.put("tags",accountService.getSumGrabTagsById(userId,queryDate));
            }
            result.setData(resultMap);
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
