package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeChatroom;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.utils.JsonUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("room")
public class RoomController {

    @Autowired
    RoomService roomService;

    /**
     * 房间列表
     */
    @PostMapping("getRoomList")
    public ResultMsg<Object> getRoomList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?0:paramJson.getInteger("pageSize");
            Integer tagId = paramJson.getInteger("tagId");

            Map<String,Object> map = new HashMap<>();
            map.put("pageInfo",roomService.getRoomList(startPage, pageSize,tagId));
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
     * 我进入的房间列表
     * @param request
     * @return
     */
    @PostMapping("getRoomLogList")
    public ResultMsg<Object> getRoomLogList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?0:paramJson.getInteger("pageSize");
            Long userId = paramJson.getLong("userId");

            Map<String,Object> map = new HashMap<>();
            map.put("pageInfo",roomService.getRoomLogList(startPage, pageSize,userId));
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
     * 获取房间信息
     * @param request
     * @return
     */
    @PostMapping("getRoomInfo")
    public ResultMsg<Object> getRoomInfo(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Long roomId = paramJson.getLong("roomId");

            if (roomId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            Map<String,Object> map = roomService.getRoomInfo(roomId);

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
     * 获取房间普通用户
     * @param request
     * @return
     */
    @PostMapping("getRoomUserById")
    public ResultMsg<Object> getRoomUserById(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
            Long roomId = paramJson.getLong("roomId");

            String nickname = paramJson.getString("nickname");
            if (roomId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("userList",roomService.getRoomUserById(startPage,pageSize,roomId,nickname)));
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
     * 获取房间所有用户
     * @param request
     * @return
     */
    @PostMapping("getRoomUserByIdAll")
    public ResultMsg<Object> getRoomUserByIdAll(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
            Long roomId = paramJson.getLong("roomId");

            String nickname = paramJson.getString("nickname");
            if (roomId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("userList",roomService.getRoomUserByIdAll(startPage,pageSize,roomId,nickname)));
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
     * 创建房间
     * @param request
     * @return
     */
    @PostMapping("roomCreate")
    public ResultMsg<Object> roomCreate(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Long userId = paramJson.getLong("userId");

            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            Long roomId = roomService.roomCreate(userId);

            result.setData(JsonUtil.getMap("roomId",roomId));

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
     * 关闭房间
     * @param request
     * @return
     */
    @PostMapping("roomClose")
    public ResultMsg<Object> roomClose(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Long userId = paramJson.getLong("userId");
            Long roomId = paramJson.getLong("roomId");
            if (userId==null||roomId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.roomClose(userId,roomId);

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
     * 设置房间
     * @param request
     * @return
     */
    @PostMapping("roomUpdate")
    public ResultMsg<Object> roomUpdate(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            FuntimeChatroom chatroom = JSONObject.toJavaObject(paramJson, FuntimeChatroom.class);

            if (chatroom.getId()==null||chatroom.getName()==null||chatroom.getTags()==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.roomUpdate(chatroom);

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
     * 加入房间
     */
    @PostMapping("roomJoin")
    public ResultMsg<Object> roomJoin(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer type = paramJson.getInteger("type");//1-被邀请进房
            Long userId = paramJson.getLong("userId");
            Long roomId = paramJson.getLong("roomId");
            String password = paramJson.getString("password");
            if (userId==null||roomId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            boolean isOwner = roomService.roomJoin(userId,roomId,password,type);

            result.setData(JsonUtil.getMap("isOwer",isOwner));

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
     * 退出房间
     */
    @PostMapping("roomExit")
    public ResultMsg<Object> roomExit(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);


            Long userId = paramJson.getLong("userId");
            Long roomId = paramJson.getLong("roomId");
            if (userId==null||roomId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.roomExit(userId,roomId);

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
     * 踢出房间
     */
    @PostMapping("roomKicked")
    public ResultMsg<Object> roomKicked(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);


            Long userId = paramJson.getLong("userId");
            Long kickIdUserId = paramJson.getLong("kickIdUserId");
            Long roomId = paramJson.getLong("roomId");
            if (userId==null||roomId==null||kickIdUserId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.roomKicked(kickIdUserId,userId,roomId);

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
     * 抱麦(发通知)
     * @param request
     * @return
     */
    @PostMapping("holdWheat")
    public ResultMsg<Object> holdWheat(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long micUserId = paramJson.getLong("micUserId");
            Long roomId = paramJson.getLong("roomId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (userId==null||micUserId==null||roomId==null||micLocation==null||micLocation<1) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.holdWheat(userId,roomId,micLocation,micUserId);

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
     * 上麦
     * @param request
     * @return
     */
    @PostMapping("upperWheat")
    public ResultMsg<Object> upperWheat(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long micUserId = paramJson.getLong("micUserId");
            Long roomId = paramJson.getLong("roomId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (micUserId==null||roomId==null||micLocation==null||micLocation<0) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.upperWheat(userId,roomId,micLocation,micUserId);

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
     * 下麦
     * @param request
     * @return
     */
    @PostMapping("lowerWheat")
    public ResultMsg<Object> lowerWheat(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long micUserId = paramJson.getLong("micUserId");
            Long roomId = paramJson.getLong("roomId");
            if (micUserId==null||roomId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.lowerWheat(userId,roomId,micUserId);

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
     * 封麦
     * @param request
     * @return
     */
    @PostMapping("stopWheat")
    public ResultMsg<Object> stopWheat(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long roomId = paramJson.getLong("roomId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (userId==null||roomId==null||micLocation==null||micLocation<1||micLocation>9) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.stopWheat(userId,roomId,micLocation);

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
     * 解封
     * @param request
     * @return
     */
    @PostMapping("openWheat")
    public ResultMsg<Object> openWheat(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long roomId = paramJson.getLong("roomId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (userId==null||roomId==null||micLocation==null||micLocation<1||micLocation>9) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.openWheat(userId,roomId,micLocation);

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
     * 禁麦
     * @param request
     * @return
     */
    @PostMapping("forbidWheat")
    public ResultMsg<Object> forbidWheat(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Long userId = paramJson.getLong("userId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (userId==null||roomId==null||micLocation==null||micLocation<1||micLocation>9) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.forbidWheat(roomId,micLocation,userId);

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
     * 解禁
     * @param request
     * @return
     */
    @PostMapping("releaseWheat")
    public ResultMsg<Object> releaseWheat(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Long userId = paramJson.getLong("userId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (roomId==null||micLocation==null||micLocation<1||micLocation>9) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.releaseWheat(roomId,micLocation,userId);

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
     * 设为主持
     * @param request
     * @return
     */
    @PostMapping("roomManage")
    public ResultMsg<Object> roomManage(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Long userId = paramJson.getLong("userId");
            Long micUserId = paramJson.getLong("micUserId");
            if (micUserId == null||roomId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.roomManage(roomId,userId,micUserId);

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
     * 取消主持
     * @param request
     * @return
     */
    @PostMapping("roomManageCancel")
    public ResultMsg<Object> roomManageCancel(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Long userId = paramJson.getLong("userId");
            Long micUserId = paramJson.getLong("micUserId");
            if (micUserId == null||roomId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            roomService.roomManageCancel(roomId,userId,micUserId);

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
     * 抽麦序
     * @param request
     * @return
     */
    @PostMapping("roomRandomMic")
    public ResultMsg<Object> roomRandomMic(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Long userId = paramJson.getLong("userId");
            Long micUserId = paramJson.getLong("micUserId");
            if (micUserId == null||roomId==null||userId == null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            int mic = roomService.roomRandomMic(roomId,userId,micUserId);

            result.setData(JsonUtil.getMap("mic",mic));
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
     * 查询参与赠送的礼物
     * @param request
     * @return
     */
    @PostMapping("getGiftListByBestowed")
    public ResultMsg<Object> getGiftListByBestowed(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {

            result.setData(JsonUtil.getMap("gifts",roomService.getGiftListByBestowed(1)));
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

    @PostMapping("getGiftList")
    public ResultMsg<Object> getGiftList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {

            result.setData(JsonUtil.getMap("gifts",roomService.getGiftList()));
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
     * 发送公屏消息
     * @param request
     * @return
     */
    @PostMapping("sendNotice")
    public ResultMsg<Object> sendNotice(HttpServletRequest request) {
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String imgUrl = paramJson.getString("imgUrl");
            String msg = paramJson.getString("msg");
            Long roomId = paramJson.getLong("roomId");
            Integer type = paramJson.getInteger("type");
            roomService.sendNotice(userId,imgUrl,msg,roomId,type);


        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }

    /**
     * 獲取騰訊聊天室用戶
     * @param request
     * @return
     */
    @PostMapping("getTencentRoomMember")
    public ResultMsg<Object> getTencentRoomMember(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String groupId = paramJson.getString("groupId");
            result.setData(TencentUtil.getGroupMemberInfo(UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER),groupId));


        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }

    /**
     * 获取用户加入的群组
     * @param request
     * @return
     */
    @PostMapping("getGoinedGroupList")
    public ResultMsg<Object> getGoinedGroupList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String userId = paramJson.getString("userId");
            result.setData(TencentUtil.getGoinedGroupList(UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER),userId));


        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }

    /**
     * 获取用户状态
     * @param request
     * @return
     */
    @PostMapping("queryState")
    public ResultMsg<Object> queryState(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String userIds = paramJson.getString("userIds");
            String[] toAccounts = userIds.split(",");

            result.setData(TencentUtil.querystate(UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER),toAccounts));


        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }


    @PostMapping("destory")
    public ResultMsg<Object> destory(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String userId = paramJson.getString("userId");
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            JSONObject goinedGroupList = TencentUtil.getGoinedGroupList(userSig, userId);
            if (goinedGroupList!=null){
                JSONArray groups = goinedGroupList.getJSONArray("GroupIdList");
                List<String> members = new ArrayList<>();
                members.add(userId);
                for (int i =0 ;i<groups.size();i++){
                    TencentUtil.deleteGroupMember(userSig,groups.getJSONObject(i).getString("GroupId"),members);
                }
            }


        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }

    /**
     * 解散群组
     * @param request
     * @return
     */
    @PostMapping("destroyGroup")
    public ResultMsg<Object> destroyGroup(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String groupId = paramJson.getString("groupId");
            result.setData(TencentUtil.destroyGroup(UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER),groupId));

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }
}
