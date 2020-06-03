package com.rzyou.funtime.game.ddz;

public enum DdzPokerType {
    //单张1，对子2，三张3，三带单4，三带对5，单顺6，双顺7，飞机8，飞机带单9，飞机带双10，四带两单11，四带对12，炸弹13，火箭14
    DDZ_PASS(0,"过牌，不出"),
    SINGLE(1,"单张"),
    TWIN(2,"对子"),
    TRIPLE (3,"三张"),
    TRIPLE_WITH_SINGLE (4,"三带单"),
    TRIPLE_WITH_TWIN (5,"三带对"),
    STRAIGHT_SINGLE(6,"单顺"),
    STRAIGHT_TWIN(7,"双顺"),
    PLANE_PURE(8,"飞机"),
    PLANE_WITH_SINGLE(9,"飞机带单"),
    PLANE_WITH_TWIN(10,"飞机带双"),
    FOUR_WITH_SINGLE(11,"四带两单"),
    FOUR_WITH_TWIN (12,"四带对"),
    FOUR_BOMB(13,"炸弹"),
    KING_BOMB(14,"火箭");

    private int value;
    private String desc;

    DdzPokerType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
