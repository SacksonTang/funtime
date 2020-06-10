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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("room")
@Slf4j
public class RoomController {

    @Autowired
    RoomService roomService;

    /**
     * 开启麦位音乐权限
     * @param request
     * @return
     */
    @PostMapping("startMusicAuth")
    public ResultMsg<Object> startMusicAuth(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (roomId==null||micLocation==null||micLocation<1||micLocation>10){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            roomService.startMusicAuth(roomId,micLocation);
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
     * 关闭麦位音乐权限
     * @param request
     * @return
     */
    @PostMapping("cancelMusicAuth")
    public ResultMsg<Object> cancelMusicAuth(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Integer micLocation = paramJson.getInteger("micLocation");
            if (roomId==null||micLocation==null||micLocation<1||micLocation>10){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            roomService.cancelMusicAuth(roomId,micLocation);
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
     * 移除管理员
     * @param request
     * @return
     */
    @PostMapping("delRoomManager")
    public ResultMsg<Object> delRoomManager(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long id = paramJson.getLong("id");
            if (id==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            roomService.delRoomManager(id);
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
     * 设置管理员
     * @param request
     * @return
     */
    @PostMapping("setRoomManager")
    public ResultMsg<Object> setRoomManager(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Integer tagId = paramJson.getInteger("tagId");
            String managerIds = paramJson.getString("managerId");
            Long userId = HttpHelper.getUserId();
            if (roomId==null||tagId == null || managerIds == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(roomService.setRoomManager(roomId,tagId,managerIds,userId));
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
     * 管理员列表
     * @param request
     * @return
     */
    @PostMapping("getRoomManagerList")
    public ResultMsg<Object> getRoomManagerList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
            if (roomId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(roomService.getRoomManagerList(roomId,startPage,pageSize));
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
     * 管理员列表
     * @param request
     * @return
     */
    @PostMapping("getDurationConfs")
    public ResultMsg<Object> getDurationConfs(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {

            result.setData(roomService.getDurationConfs());
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


    @PostMapping("sendRoomInfoNotice")
    public ResultMsg<Object> sendRoomInfoNotice(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long roomId = paramJson.getLong("roomId");
            if (roomId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            roomService.sendRoomInfoNotice(roomId);
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
     * 背景列表
     * @param request
     * @return
     */
    @PostMapping("getBackgroundList")
    public ResultMsg<Object> getBackgroundList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?30:paramJson.getInteger("pageSize");
            Long userId = paramJson.getLong("userId");
            if (userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> map = new HashMap<>();
            map.put("pageInfo",roomService.getBackgroundList(startPage, pageSize,userId));
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
     * 购买背景图
     * @param request
     * @return
     */
    @PostMapping("buyBackground")
    public ResultMsg<Object> buyBackground(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer backgroundId = paramJson.getInteger("backgroundId");
            Long userId = paramJson.getLong("userId");
            if (backgroundId==null||userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            return roomService.buyBackground(backgroundId,userId);
        } catch (BusinessException be) {
            log.error("buyBackground BusinessException==========>{}",be.getMsg());
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
     * 设置背景
     * @param request
     * @return
     */
    @PostMapping("setBackground")
    public ResultMsg<Object> setBackground(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer backgroundId = paramJson.getInteger("backgroundId");
            Long userId = paramJson.getLong("userId");
            Long roomId = paramJson.getLong("roomId");
            if (backgroundId==null||userId==null||roomId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            roomService.setBackground(backgroundId,userId,roomId);
            return result;
        } catch (BusinessException be) {
            log.error("setBackground BusinessException==========>{}",be.getMsg());
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
     * 房间列表
     */
    @PostMapping("getRoomList")
    public ResultMsg<Object> getRoomList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
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

            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
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
            Long userId = paramJson.getLong("userId");

            if (roomId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if(userId == null){
                userId = HttpHelper.getUserId();
            }

            Map<String,Object> map = roomService.getRoomInfo(roomId,userId);

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
            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
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
            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
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
     * 获取房间所有用户(管理员)
     * @param request
     * @return
     */
    @PostMapping("getRoomUserByIdAll2")
    public ResultMsg<Object> getRoomUserByIdAll2(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
            Long roomId = paramJson.getLong("roomId");
            if (roomId==null||userId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("userList",roomService.getRoomUserByIdAll2(startPage,pageSize,roomId,userId)));
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
            Integer platform = paramJson.getInteger("platform");
            platform = platform == null?1:platform;
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(roomService.roomCreate(userId,platform));
            return result;
        } catch (BusinessException be) {
            log.error("roomCreate BusinessException==========>{}",be.getMsg());
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
            log.error("roomClose BusinessException==========>{}",be.getMsg());
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
            log.error("roomUpdate BusinessException==========>{}",be.getMsg());
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
            result.setData(roomService.roomJoin(userId,roomId,password,type));
            return result;
        } catch (BusinessException be) {
            log.error("roomJoin BusinessException==========>{}",be.getMsg());
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
            log.error("roomExit BusinessException==========>{}",be.getMsg());
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
            log.error("roomKicked BusinessException==========>{}",be.getMsg());
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
            log.error("holdWheat BusinessException==========>{}",be.getMsg());
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
            log.error("upperWheat BusinessException==========>{}",be.getMsg());
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
            log.error("lowerWheat BusinessException==========>{}",be.getMsg());
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
            log.error("stopWheat BusinessException==========>{}",be.getMsg());
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
            log.error("openWheat BusinessException==========>{}",be.getMsg());
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
            log.error("forbidWheat BusinessException==========>{}",be.getMsg());
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
            log.error("releaseWheat BusinessException==========>{}",be.getMsg());
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
    //@PostMapping("roomManage")
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
    //@PostMapping("roomManageCancel")
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

    @PostMapping("getGiftListInit")
    public ResultMsg<Object> getGiftListInit(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {

            result.setData(JsonUtil.getMap("gifts",roomService.getGiftListInit()));
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
            Integer playLenth = paramJson.getInteger("playLength");
            String msg = paramJson.getString("msg");
            Long roomId = paramJson.getLong("roomId");
            Integer type = paramJson.getInteger("type");
            roomService.sendNotice(userId,imgUrl,msg,roomId,type,playLenth);


        } catch (BusinessException be) {
            log.error("sendNotice BusinessException==========>{}",be.getMsg());
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



}
