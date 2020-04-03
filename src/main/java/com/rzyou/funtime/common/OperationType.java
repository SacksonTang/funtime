package com.rzyou.funtime.common;

public enum OperationType {

     RECHARGE("IN","RECHARGE","充值")
    ,GRABREDPACKET("IN","GRABREDPACKET","抢红包")
    ,REDPACKETINVALID("IN","REDPACKETINVALID","红包过期退回")
    ,RECEIVEGIFT("IN","RECEIVEGIFT","收礼物")
    ,GAMEIN("IN","GAMEIN","游戏收入")
    ,BLACK_BLUE_IN("IN","BLACK_BLUE_IN","黑钻兑换蓝钻")
    ,WITHDRAWAL_RETURN("IN","WITHDRAWAL_RETURN","提现退回")
    ,YAOYAOLE_IN("IN","YAOYAOLE_IN","摇摇乐中奖")

    ,WITHDRAWAL("OUT","WITHDRAWAL","提现")
    ,GIVEREDPACKET("OUT","GIVEREDPACKET","发红包")
    ,GIVEGIFT("OUT","GIVEGIFT","送礼物")
    ,GAMEOUT("OUT","GAMEOUT","游戏支出")
    ,BLACK_BLUE_OUT("OUT","BLACK_BLUE_OUT","黑钻兑换蓝钻")
    ,YAOYAOLE_OUT("OUT","YAOYAOLE_OUT","摇摇乐消耗")
    ,BUY_BACKGROUND("OUT","BUY_BACKGROUND","购买背景资源")
    ,BUY_HORN("OUT","BUY_HORN","购买喇叭")
    ,HORN_CONSUME("OUT","HORN_CONSUME","发送大喇叭")
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
