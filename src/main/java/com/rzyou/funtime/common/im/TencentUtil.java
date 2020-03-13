package com.rzyou.funtime.common.im;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.httputil.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
@Slf4j
public class TencentUtil {

    /**
     * 获取群组用户信息
     * @param usersig
     * @param groupId
     * @return
     */
    public static JSONObject getGroupMemberInfo(String usersig,String groupId){
        String url = getGroupUrl(Constant.TENCENT_YUN_GET_GROUP_MENBER_INFO,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("Name","funtime");
        paramMap.put("Type","ChatRoom");
        paramMap.put("GroupId",groupId);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        return result;

    }

    /**
     * 获取用户加入的群组
     * @param usersig
     * @param userId
     * @return
     */
    public static JSONObject getGoinedGroupList(String usersig,String userId){
        String url = getGroupUrl(Constant.TENCENT_YUN_GET_JOINED_GROUP_LIST,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("Member_Account",userId);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        return result;

    }


    /**
     * 创建聊天室
     * @param usersig
     * @return
     */
    public static boolean createGroup(String usersig,List<Map<String, String>> memberList,String groupId){
        String url = getGroupUrl(Constant.TENCENT_YUN_CREATE_GROUP,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("Name","funtime");
        paramMap.put("Type","ChatRoom");
        paramMap.put("GroupId",groupId);
        paramMap.put("MemberList",memberList);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){
            log.error("groupId:{}",groupId);
            log.info("腾讯新建组接口:create_group 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
            return false;
        }else{
            log.debug("*************腾讯新建组接口:create_group 调用成功************");
            return true;
        }

    }



    /**
     * 解散房间
     * @param usersig
     * @param groupId
     * @return
     */
    public static boolean destroyGroup(String usersig, String groupId){
        String url = getGroupUrl(Constant.TENCENT_YUN_DESTROY_GROUP,usersig);

        JSONObject paramMap = new JSONObject();
        paramMap.put("GroupId",groupId);
        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){
            log.error("groupId:{}",groupId);
            log.info("腾讯解散组接口:destroy_group 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
            return false;
        }else{
            log.debug("*************腾讯解散组接口:destroy_group 调用成功************");
            return true;
        }
    }

    /**
     * 添加用户
     * @param usersig
     * @param groupId
     * @param memberList
     * @return
     */
    public static JSONArray addGroupMember(String usersig, String groupId, List<Map<String, String>> memberList){
        String url = getGroupUrl(Constant.TENCENT_YUN_ADD_GROUP_MEMBER,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("Silence",1);
        paramMap.put("GroupId",groupId);
        paramMap.put("MemberList",memberList);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){
            log.debug("groupId:{}",groupId);
            log.error("腾讯添加组用户接口:add_group_member 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
            return result.getJSONArray("MemberList");
        }else{
            log.debug("*********腾讯添加组用户接口:add_group_member 调用成功*************");
            return result.getJSONArray("MemberList");
        }
    }

    /**
     * 删除用户
     * @param usersig
     * @param groupId
     * @param memberList
     * @return
     */
    public static boolean deleteGroupMember(String usersig,String groupId,List<String> memberList){
        String url = getGroupUrl(Constant.TENCENT_YUN_DELETE_GROUP_MEMBER,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("Silence",1);
        paramMap.put("GroupId",groupId);
        paramMap.put("MemberToDel_Account",memberList);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){

            log.debug("groupId:{}",groupId);
            log.error("腾讯删除组用户接口:delete_group_member 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
            return false;
        }else{
            log.debug("***************腾讯删除组用户接口:delete_group_member 调用成功************");
            return true;
        }
    }

    /**
     * 聊天室发生普通消息
     * @param usersig

     * @return
     */
    public static boolean sendGroupMsg(String usersig,String paramMap){
        String url = getGroupUrl(Constant.TENCENT_YUN_SEND_GROUP_MSG,usersig);

        /*
        JSONObject paramMap = new JSONObject();
        Random random = new Random();
        paramMap.put("OnlineOnlyFlag",1);
        paramMap.put("GroupId",groupId);
        paramMap.put("Random",random.nextInt(100000000));
        Map<String,String> msgContent = new HashMap<>();
        msgContent.put("Data",data);
        msgContent.put("Desc","");
        msgContent.put("Ext","");
        msgContent.put("Sound","");
        Map<String,Object> elem = new HashMap<>();
        elem.put("MsgType","TIMCustomElem");
        elem.put("MsgContent",msgContent);
        List<Map<String,Object>> msgBody = new ArrayList<>();
        msgBody.add(elem);
        paramMap.put("MsgBody",msgBody);*/

        log.debug("paramMap:{}",paramMap);
        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){

            log.error("腾讯发送普通消息接口:send_group_msg 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
            return false;
        }else{
            log.debug("************腾讯发送普通消息接口:send_group_msg 调用成功*******************");
            return true;
        }
    }

    /**
     * 批量发单聊消息
     * @param usersig
     * @param toAccounts
     * @param data
     * @return
     */
    public static JSONArray batchsendmsg(String usersig,List<String> toAccounts,String data){
        String url = getOpenimUrl(Constant.TENCENT_YUN_BATCHSENDMSG,usersig);

        JSONObject paramMap = new JSONObject();
        Random random = new Random();
        paramMap.put("SyncOtherMachine",2);
        paramMap.put("MsgLifeTime",0);
        paramMap.put("To_Account",toAccounts);
        paramMap.put("MsgRandom",random.nextInt(100000000));
        Map<String,String> msgContent = new HashMap<>();
        msgContent.put("Data",data);
        msgContent.put("Desc","");
        msgContent.put("Ext","");
        msgContent.put("Sound","");
        Map<String,Object> elem = new HashMap<>();
        elem.put("MsgType","TIMCustomElem");
        elem.put("MsgContent",msgContent);
        List<Map<String,Object>> msgBody = new ArrayList<>();
        msgBody.add(elem);
        paramMap.put("MsgBody",msgBody);

        log.debug(JSONObject.toJSONString(paramMap));
        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){

            log.error("腾讯批量发单聊接口:batchsendmsg 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
            return result.getJSONArray("ErrorList");
        }else{
            log.debug("************腾讯批量发单聊接口:batchsendmsg 调用成功*******************");
            return null;
        }
    }

    /**
     * 单聊消息
     * @param usersig
     * @param paramMap
     * @return
     */
    public static boolean sendMsg(String usersig,String paramMap){
        String url = getOpenimUrl(Constant.TENCENT_YUN_SENDMSG,usersig);
        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){
            log.error("腾讯发单聊接口:sendmsg 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
            return false;
        }else{
            log.debug("************腾讯发单聊接口:sendmsg 调用成功*******************");
            return true;
        }
    }

    /**
     * 查询用户状态
     * @param usersig
     * @param toAccounts
     * @return
     */
    public static JSONArray querystate(String usersig,String[] toAccounts){
        String url = getOpenimUrl(Constant.TENCENT_YUN_QUERYSTATE,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("To_Account",toAccounts);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){

            log.error("腾讯批量发单聊接口:querystate 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));
        }else{
            log.debug("************腾讯批量发单聊接口:querystate 调用成功*******************");
            return result.getJSONArray("QueryResult");
        }
        return null;
    }


    /**
     * 系统通知
     * @param usersig
     * @return
     */
    public static boolean sendGroupSystemNotification(String usersig,String paramMap){
        String url = getGroupUrl(Constant.TENCENT_YUN_SEND_SYSTEM_NOTIFICATION,usersig);
        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){
            log.error("腾讯系统通知接口:send_group_system_notification 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));

            return false;
        }else{
            log.debug("**************腾讯系统通知接口:send_group_system_notification 调用成功*****************");
            return true;
        }
    }

    /**
     * 用户导入
     * @param usersig
     * @param userId
     * @param nickname
     * @param faceUrl
     * @return
     */
    public static boolean accountImport(String usersig,String userId,String nickname,String faceUrl){
        String url = getImUrl(Constant.TENCENT_YUN_ACCOUNT_IMPORT,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("Identifier",userId);
        paramMap.put("Nick",nickname);
        paramMap.put("FaceUrl",faceUrl);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){

            log.info("腾讯导入用户接口:account_import 调用出错,ErrorCode：{},ErrorInfo:{}",result.getString("ErrorCode"),result.getString("ErrorInfo"));

            return false;
        }else{
            return true;
        }
    }

    /**
     * 设置用户资料
     * @param usersig
     * @param userId
     * @param nickname
     * @param faceUrl
     * @return
     */
    public static boolean portraitSet(String usersig,String userId,String nickname,String faceUrl,String sex){
        String url = getPortraitUrl(Constant.TENCENT_YUN_PORTRAIT_SET,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("From_Account",userId);

        List<Map<String,Object>> profileItems = new ArrayList<>();
        JSONObject profileItem = new JSONObject();
        if (StringUtils.isNotBlank(nickname)) {
            profileItem.put("Tag", "Tag_Profile_IM_Nick");
            profileItem.put("Value", nickname);
            profileItems.add(profileItem);
        }
        if (StringUtils.isNotBlank(faceUrl)) {
            profileItem = new JSONObject();
            profileItem.put("Tag", "Tag_Profile_IM_Image");
            profileItem.put("Value", faceUrl);
            profileItems.add(profileItem);
        }
        if (StringUtils.isNotBlank(sex)) {

            profileItem = new JSONObject();
            profileItem.put("Tag", "Tag_Profile_IM_Gender");
            if (sex.equals("1")) {
                profileItem.put("Value", "Gender_Type_Male");
            }else{
                profileItem.put("Value", "Gender_Type_Female");
            }
            profileItems.add(profileItem);
        }

        paramMap.put("ProfileItem",profileItems);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){

            log.info("腾讯设置用户资料接口:portrait_set 调用出错,ErrorCode：{},ErrorInfo:{},paramMap:{}",result.getString("ErrorCode")
                    ,result.getString("ErrorInfo"),JSONObject.toJSONString(paramMap));
            return false;
        }else{
            return true;
        }
    }

    /**
     * 设置等级URL
     * @param usersig
     * @param userId
     * @param levelUrl
     * @return
     */
    public static boolean portraitSet(String usersig,String userId,Integer level,String levelUrl){
        String url = getPortraitUrl(Constant.TENCENT_YUN_PORTRAIT_SET,usersig);

        JSONObject paramMap = new JSONObject();

        paramMap.put("From_Account",userId);

        List<Map<String,Object>> profileItems = new ArrayList<>();
        JSONObject profileItem = new JSONObject();
        if (StringUtils.isNotBlank(levelUrl)) {
            profileItem.put("Tag", "Tag_Profile_IM_SelfSignature");
            profileItem.put("Value", levelUrl);
            profileItems.add(profileItem);
        }
        if (level!=null) {
            profileItem = new JSONObject();
            profileItem.put("Tag", "Tag_Profile_IM_Level");
            profileItem.put("Value", level);
            profileItems.add(profileItem);
        }
        paramMap.put("ProfileItem",profileItems);

        String postStr = HttpClientUtil.doPost(url, paramMap, Constant.CONTENT_TYPE);
        JSONObject result = JSONObject.parseObject(postStr);
        if (!"OK".equals(result.getString("ActionStatus"))||result.getInteger("ErrorCode")!=0){

            log.info("腾讯设置用户资料接口:portrait_set 调用出错,ErrorCode：{},ErrorInfo:{},paramMap:{}",result.getString("ErrorCode")
                    ,result.getString("ErrorInfo"),JSONObject.toJSONString(paramMap));
            return false;
        }else{
            return true;
        }
    }


    public static String getOpenimUrl(String command,String usersig){

        return getUrl(Constant.TENCENT_YUN_SERVICENAME_OPENIM,command,usersig);
    }

    public static String getGroupUrl(String command,String usersig){

        return getUrl(Constant.TENCENT_YUN_SERVICENAME_GROUP,command,usersig);
    }
    public static String getImUrl(String command,String usersig){

        return getUrl(Constant.TENCENT_YUN_SERVICENAME_IM,command,usersig);
    }
    public static String getPortraitUrl(String command,String usersig){

        return getUrl(Constant.TENCENT_YUN_SERVICENAME_PORTRAIT,command,usersig);
    }


    public static String getUrl(String servicename,String command,String usersig){

        String url = "https://HOST/VER/SERVICENAME/COMMAND?sdkappid=APPID&identifier=IDENTIFIER&usersig=USERSIG&random=RANDOM&contenttype=json";

        Random random = new Random();

        url = url.replaceAll("VER", Constant.TENCENT_YUN_SDK_VER)
                .replaceAll("HOST",Constant.TENCENT_YUN_SDK_HOST)
                .replaceAll("SERVICENAME",servicename)
                .replaceAll("COMMAND",command)
                .replaceAll("APPID",String.valueOf(Constant.TENCENT_YUN_SDK_APPID))
                .replaceAll("IDENTIFIER",Constant.TENCENT_YUN_IDENTIFIER)
                .replaceAll("USERSIG",usersig)
                .replaceAll("RANDOM",String.valueOf(random.nextInt(100000000)));

        return url;
    }




}
