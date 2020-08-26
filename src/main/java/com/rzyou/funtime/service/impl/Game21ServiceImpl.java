package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.FuntimeRoomGame21;
import com.rzyou.funtime.game.PokerStaticData;
import com.rzyou.funtime.mapper.FuntimeRoomGame21Mapper;
import com.rzyou.funtime.service.Game21Service;
import com.rzyou.funtime.service.NoticeService;
import com.rzyou.funtime.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class Game21ServiceImpl implements Game21Service {

    @Autowired
    RoomService roomService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    FuntimeRoomGame21Mapper roomGame21Mapper;

    final static String ROOM_MIC_NUM = "game21_roommic_num_";
    final static String ROOM_POKER_NUM = "game21_roompoker_num_";
    final static String ROOM_POKER_STATE = "game21_roompoker_state_";

    //Map<Long,List<Integer>> game21Map = new HashMap<>();
    Map<Integer, String> pokerMap = PokerStaticData.game21PokerMap;
    Map<String, Integer> pokerVal = PokerStaticData.game21pokerVal;


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> startGame(Long userId, Long roomId) {
        log.info("开始游戏,第一轮==============>>>>>>>>>");
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> mics = roomService.getMicInfoByRoomId(roomId);
        if (mics == null&&mics.size()==0){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME21_MIC_EMPTY.getValue(),ErrorMsgEnum.ROOM_GAME21_MIC_EMPTY.getDesc());
        }
        if (roomGame21Mapper.getRoundsByRoomId(roomId)!=null){
            roomGame21Mapper.deleteGame(roomId);
            roomGame21Mapper.deleteGame2(roomId);
        }
        List<Integer> pokerNumber = new ArrayList<>(PokerStaticData.game21PokerNumber);
        log.info("startGame pkerNumer:{}",pokerNumber);
        //乱序
        Collections.shuffle(pokerNumber);
        List<FuntimeRoomGame21> list = new ArrayList<>();
        FuntimeRoomGame21 roomGame21 ;
        for (Map<String, Object> map:mics){
            roomGame21 = new FuntimeRoomGame21();
            roomGame21.setRoomId(roomId);
            roomGame21.setMicLocation(Integer.parseInt(map.get("micLocation").toString()));
            roomGame21.setUserId(Long.parseLong(map.get("micUserId").toString()));
            roomGame21.setPokerNum(1);
            roomGame21.setPokers(pokerMap.get(pokerNumber.get(0)));
            roomGame21.setState(0);
            roomGame21.setCounts(getCountPoints(roomGame21.getPokers()));
            list.add(roomGame21);
            pokerNumber.remove(0);
        }

        int k = roomGame21Mapper.insertBatch(list);
        if (k<1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        long timestamp = System.currentTimeMillis()+25000;
        String timeZone = TimeZone.getDefault().getID();
        resultMap.put("timestamp",25);
        resultMap.put("stamp",timestamp);
        resultMap.put("timeZone",timeZone);
        k = roomGame21Mapper.insertGame(roomId,timestamp);
        if (k<1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        redisUtil.set(ROOM_POKER_NUM+roomId,pokerNumber,60*60*24);
        //game21Map.put(roomId,pokerNumber);
        log.info("pokerNumber : {}",pokerNumber.size());
        resultMap.put("mics",list);
        resultMap.put("rounds",1);
        List<FuntimeRoomGame21> totalmics = roomGame21Mapper.getGameInfoByRoomId(roomId);
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20001(userIds,list,25,  1,timestamp, totalmics,timeZone);
        }

        //游戏要牌人数缓存
        redisUtil.set(ROOM_MIC_NUM+roomId,list.size(),60*60*24);
        log.info("开始游戏,第一轮结束==============>>>>>>>>>游戏人数：{}",list.size());
        return resultMap;
    }

    public Map<String, Object> sendPoker(Long roomId) {
        log.info("发牌开始==============>>>>>>>>>");
        //要牌的麦位
        List<FuntimeRoomGame21> game21List = roomGame21Mapper.getGameInfoForStateByRoomId(roomId);
        Integer rounds = roomGame21Mapper.getRoundsByRoomId(roomId);
        if (game21List == null||game21List.size()==0){
            return gameOver(roomId,rounds+1);
        }

        Map<String, Object> resultMap = new HashMap<>();
        List<Integer> redisPokerNumer = (List<Integer>) redisUtil.get(ROOM_POKER_NUM+roomId);
        List<Integer> pokerNumber = redisPokerNumer==null?PokerStaticData.pokerNumber:redisPokerNumer;
        //乱序
        Collections.shuffle(pokerNumber);

        List<FuntimeRoomGame21> list = new ArrayList<>();
        Integer state3 = 0;


        for (FuntimeRoomGame21 roomGame21 : game21List) {
            roomGame21.setPokerNum(1+roomGame21.getPokerNum());
            roomGame21.setPokers(roomGame21.getPokers()+","+pokerMap.get(pokerNumber.get(0)));
            roomGame21.setCounts(getCountPoints(roomGame21.getPokers()));
            if (roomGame21.getCounts() >21){
                roomGame21.setState(3);
                state3++;
            }else {
                roomGame21.setState(0);
            }
            roomGame21Mapper.updateGameInfo(roomGame21);
            list.add(roomGame21);
            pokerNumber.remove(0);

        }
        redisUtil.set(ROOM_POKER_NUM+roomId,pokerNumber,60*60*24);

        long timestamp = System.currentTimeMillis()+25000;
        if(state3 == game21List.size()){

            return gameOver(roomId,list,rounds+1);
        }

        String timeZone = TimeZone.getDefault().getID();
        //game21Map.put(roomId,pokerNumber);
        log.info("pokerNumber : {}",pokerNumber.size());
        resultMap.put("mics",list);
        resultMap.put("timestamp",25);
        resultMap.put("stamp",timestamp);
        resultMap.put("timeZone",timeZone);
        resultMap.put("rounds",1+rounds);

        roomGame21Mapper.updateGame(roomId,timestamp,1);
        List<FuntimeRoomGame21> totalmics = roomGame21Mapper.getGameInfoByRoomId(roomId);
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20001(userIds,list,25,rounds+1, timestamp,totalmics, timeZone);
        }

        //游戏要牌人数缓存
        redisUtil.set(ROOM_MIC_NUM+roomId,game21List.size()-state3,60*60*24);
        log.info("发牌结束==============>>>>>>>>>正常人数：{}",game21List.size()-state3);
        return resultMap;
    }

    public Integer getCountPoints(String pokers){
        String[] pokerArr = pokers.split(",");
        List<Integer> pokerVals = new ArrayList<>();
        for (String poker:pokerArr){
            pokerVals.add(pokerVal.get(poker));
        }

        if (pokerVals.contains(1)){
            pokerVals.remove(new Integer(1));
            if (pokerVals.size()>0){
                Integer total = 0;
                for (Integer val : pokerVals){
                    total+=val;
                }
                if (total>10){

                    return total+1;
                }else{
                    return total+11;
                }
            }else{
                return 11;
            }
        }else{
            Integer total = 0;
            for (Integer val : pokerVals){
                total+=val;
            }

            return  total;
        }


    }

    public Map<String,Object> gameOver(Long roomId,int rounds){
        log.info("gameover:------");
        List<Map<String,Object>>  wins = roomGame21Mapper.getGameWinUserByRoomId(roomId);
        Map<String,Object> resultMap = new HashMap<>();
        List<FuntimeRoomGame21> totalmics = roomGame21Mapper.getGameInfoByRoomId(roomId);
        resultMap.put("wins",wins);
        resultMap.put("rounds",rounds);
        resultMap.put("totalmics",totalmics);
        log.info("gameover=================result:{}",resultMap);
        roomGame21Mapper.deleteGame(roomId);
        roomGame21Mapper.deleteGame2(roomId);
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20005(userIds,wins,totalmics, null,rounds);
        }
        redisUtil.del(ROOM_MIC_NUM+roomId);
        redisUtil.del(ROOM_POKER_NUM+roomId);
        return resultMap;
    }
    public Map<String,Object> gameOver(Long roomId,List<FuntimeRoomGame21> mics,int rounds){
        log.info("gameover:------");
        List<Map<String,Object>>  wins = roomGame21Mapper.getGameWinUserByRoomId(roomId);
        Map<String,Object> resultMap = new HashMap<>();

        List<FuntimeRoomGame21> totalmics = roomGame21Mapper.getGameInfoByRoomId(roomId);
        resultMap.put("wins",wins);
        resultMap.put("rounds",rounds);
        resultMap.put("totalmics",totalmics);
        resultMap.put("mics",mics);
        log.info("gameover=================result:{}",resultMap);
        roomGame21Mapper.deleteGame(roomId);
        roomGame21Mapper.deleteGame2(roomId);
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20005(userIds,wins,totalmics,mics, rounds);
        }
        redisUtil.del(ROOM_MIC_NUM+roomId);
        redisUtil.del(ROOM_POKER_NUM+roomId);
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void exitGame(Long roomId) {
        roomGame21Mapper.deleteGame(roomId);
        roomGame21Mapper.deleteGame2(roomId);
        redisUtil.del(ROOM_MIC_NUM+roomId);
        redisUtil.del(ROOM_POKER_NUM+roomId);
        redisUtil.del(ROOM_POKER_STATE+roomId);
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20002(userIds);
        }
    }

    public void exitGameForRoomClose(Long roomId) {
        roomGame21Mapper.deleteGame(roomId);
        roomGame21Mapper.deleteGame2(roomId);
        redisUtil.del(ROOM_MIC_NUM+roomId);
        redisUtil.del(ROOM_POKER_NUM+roomId);
        redisUtil.del(ROOM_POKER_STATE+roomId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> doGetPoker(Long userId, Long roomId, Integer micLocation) {
        FuntimeRoomGame21 micInfo = roomGame21Mapper.getGameInfoByRoomIdAndMic(roomId,micLocation);
        if (micInfo == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME21_MIC_ERROR.getValue(),ErrorMsgEnum.ROOM_GAME21_MIC_ERROR.getDesc());
        }
        if (micInfo.getState()!=0){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME21_STATE_ERROR.getValue(),ErrorMsgEnum.ROOM_GAME21_STATE_ERROR.getDesc());
        }
        FuntimeRoomGame21 roomGame21 = new FuntimeRoomGame21();
        roomGame21.setId(micInfo.getId());
        //要牌
        roomGame21.setState(1);

        int k = roomGame21Mapper.updateGameInfo(roomGame21);
        if (k<1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20003(userIds, micLocation);
        }
        if(redisUtil.decr(ROOM_MIC_NUM+roomId,1)==0){

            //最后一个,又一轮发牌
            return sendPoker(roomId);


        }
        return null;

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> doStopPoker(Long userId, Long roomId, Integer micLocation) {
        FuntimeRoomGame21 micInfo = roomGame21Mapper.getGameInfoByRoomIdAndMic(roomId,micLocation);
        if (micInfo == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME21_MIC_ERROR.getValue(),ErrorMsgEnum.ROOM_GAME21_MIC_ERROR.getDesc());
        }
        FuntimeRoomGame21 roomGame21 = new FuntimeRoomGame21();
        roomGame21.setId(micInfo.getId());

        //停牌
        roomGame21.setState(2);


        int k = roomGame21Mapper.updateGameInfo(roomGame21);
        if (k<1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20004(userIds, micLocation);
        }
        if(redisUtil.decr(ROOM_MIC_NUM+roomId,1)==0){

            //最后一个,又一轮发牌
            return sendPoker(roomId);


        }
        return null;

    }

    @Override
    public void game21Task(long time) {
        List<FuntimeRoomGame21> list = roomGame21Mapper.getTaskRoomId(time);
        if (list!=null&&list.size()>0){
            for (FuntimeRoomGame21 roomGame21 : list){
                try {
                    log.info("定时任务停牌roomId:{},mic:{}",roomGame21.getRoomId(),roomGame21.getMicLocation());
                    doStopPoker(roomGame21.getUserId(), roomGame21.getRoomId(), roomGame21.getMicLocation());
                }catch (Exception e){
                    log.error("game21Task error roomId:{},mic:{}",roomGame21.getRoomId(),roomGame21.getMicLocation());
                }
            }
        }
    }

    @Override
    public Map<String, Object> getGameInfo(Long userId, Long roomId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> gameInfo = roomGame21Mapper.getGameInfo(roomId);
        resultMap.put("state",redisUtil.get(ROOM_POKER_STATE+roomId));
        if (gameInfo!=null) {
            if (gameInfo.get("taskTime")!=null){
                long k = Long.parseLong(gameInfo.get("taskTime").toString());
                gameInfo.put("timestamp",(k-System.currentTimeMillis())/1000);
                gameInfo.put("stamp",gameInfo.get("taskTime").toString());
            }

            resultMap.put("game", gameInfo);
            resultMap.put("mics", roomGame21Mapper.getGameInfoByRoomId(roomId));
        }
        return resultMap;
    }

    @Override
    public Long getUserByRoomAndMic(Long roomId, Integer mic) {
        return roomGame21Mapper.getUserByRoomAndMic(roomId, mic);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void openGame(Long userId, Long roomId) {
        List<Map<String, Object>> mics = roomService.getMicInfoByRoomId(roomId);
        if (mics == null&&mics.size()==0){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME21_MIC_EMPTY.getValue(),ErrorMsgEnum.ROOM_GAME21_MIC_EMPTY.getDesc());
        }
        if (!redisUtil.setIfAbsent(ROOM_POKER_STATE+roomId,1,60*60*24)){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME21_EXISTS.getValue(),ErrorMsgEnum.ROOM_GAME21_EXISTS.getDesc());
        }
        if (roomGame21Mapper.getRoundsByRoomId(roomId)!=null){
            roomGame21Mapper.deleteGame(roomId);
            roomGame21Mapper.deleteGame2(roomId);
        }
        ;
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice20000(userIds);
        }

    }

    public void delMicInfoForlowerWheat(Long roomId, Integer mic){
        boolean flag = false;
        List<FuntimeRoomGame21> list = roomGame21Mapper.getGameInfoByRoomId(roomId);
        if (list!=null&&list.size()>0){
            for (FuntimeRoomGame21 roomGame21 : list){
                if (roomGame21.getMicLocation().equals(mic)){
                    flag = true;
                    roomGame21Mapper.deleteGameById(roomGame21.getId());
                    break;
                }
            }
            long decr = redisUtil.decr(ROOM_MIC_NUM + roomId, 1);
            if (flag&&list.size()==1){
                exitGame(roomId);
            }else if(decr==0){

                //最后一个,又一轮发牌
                sendPoker(roomId);
            }

        }

    }
}
