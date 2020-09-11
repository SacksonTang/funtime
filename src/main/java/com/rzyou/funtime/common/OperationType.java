package com.rzyou.funtime.common;

public enum OperationType {

     RECHARGE("IN","RECHARGE","充值")
    ,ALIPAYRECHARGE("IN","ALIPAYRECHARGE","ALIPAY充值")
    ,IOSRECHARGE("IN","IOSRECHARGE","IOS充值")
    ,GRABREDPACKET("IN","GRABREDPACKET","抢红包")
    ,REDPACKETINVALID("IN","REDPACKETINVALID","红包过期退回")
    ,RECEIVEGIFT("IN","RECEIVEGIFT","收礼物")
    ,RECEIVEGIFTREDPACKET("IN","RECEIVEGIFTREDPACKET","收红包赠送礼物")
    ,RECEIVEGIFTACTIVITY("IN","RECEIVEGIFTACTIVITY","礼物活动赠送")
    ,RECEIVEBAGGIFT("IN","RECEIVEBAGGIFT","收背包礼物")
    ,GAMEIN("IN","GAMEIN","游戏收入")
    ,BLACK_BLUE_IN("IN","BLACK_BLUE_IN","黑钻兑换蓝钻")
    ,WITHDRAWAL_RETURN("IN","WITHDRAWAL_RETURN","提现退回")
    ,YAOYAOLE_IN("IN","YAOYAOLE_IN","摇摇乐中奖")
    ,SMASHEGG_IN("IN","SMASHEGG_IN","砸蛋中奖")
    ,CIRCLE_IN("IN","CIRCLE_IN","转盘中奖")
    ,GOLD_CONVERT_IN("IN","GOLD_CONVERT_IN","金币兑换")
    ,GOLD_SIGN_IN("IN","GOLD_SIGN_IN","签到")
    ,GIFT_BOX_IN("IN","GIFT_BOX_IN","宝箱收")
    ,GIFT_KNAPSACK_EGG_IN("IN","GIFT_KNAPSACK_EGG_IN","砸蛋礼物")
    ,GIFT_KNAPSACK_CIRCLE_IN("IN","GIFT_KNAPSACK_CIRCLE_IN","夺宝礼物")
    ,DDZ_GOLD_IN("IN","DDZ_GOLD_IN","斗地主")

    ,WITHDRAWAL("OUT","WITHDRAWAL","提现")
    ,GIVEREDPACKET("OUT","GIVEREDPACKET","发红包")
    ,GIVEGIFT("OUT","GIVEGIFT","送礼物")
    ,GIVEGIFTREDPACKET("OUT","GIVEGIFTREDPACKET","礼物红包赠送")
    ,GIVEGIFTACTIVITY("OUT","GIVEGIFTACTIVITY","礼物活动赠送")
    ,GIVEGIFTBAG("OUT","GIVEGIFTBAG","背包送礼物")
    ,GAMEOUT("OUT","GAMEOUT","游戏支出")
    ,BLACK_BLUE_OUT("OUT","BLACK_BLUE_OUT","黑钻兑换蓝钻")
    ,YAOYAOLE_OUT("OUT","YAOYAOLE_OUT","摇摇乐消耗")
    ,SMASHEGG_OUT("OUT","SMASHEGG_OUT","砸蛋消耗")
    ,CIRCLE_OUT("OUT","CIRCLE_OUT","夺宝消耗")
    ,BUY_BACKGROUND("OUT","BUY_BACKGROUND","购买背景资源")
    ,BUY_CAR("OUT","BUY_CAR","购买坐骑")
    ,BUY_HEADWEAR("OUT","BUY_HEADWEAR","购买头饰")
    ,BUY_HORN("OUT","BUY_HORN","购买喇叭")
    ,HORN_CONSUME("OUT","HORN_CONSUME","发送大喇叭")
    ,BUY_BULLET("OUT","BUY_BULLET","捕鱼购买子弹")
    ,GOLD_CONVERT_OUT("OUT","GOLD_CONVERT_OUT","金币兑换")
    ,GIFT_BOX_OUT("OUT","GIFT_BOX_OUT","宝箱送")
    ,GIFT_KNAPSACK_OUT("OUT","GIFT_KNAPSACK_OUT","背包送出")
    ,DDZ_GOLD_OUT("OUT","DDZ_GOLD_OUT","斗地主")
    ;

     private String action;
     private String operationType;
     private String operationDesc;

    OperationType(String action, String operationType, String operationDesc) {
        this.action = action;
        this.operationType = operationType;
        this.operationDesc = operationDesc;
    }

    public static String getAction(String operationType){
        for (OperationType operationType1:values()){
            if(operationType.equals(operationType1.getOperationType())){
                return operationType1.getAction();
            }
        }
        return null;
    }

    OperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }
}
