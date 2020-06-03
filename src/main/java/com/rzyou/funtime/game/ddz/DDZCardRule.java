package com.rzyou.funtime.game.ddz;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 斗地主
 */
@Slf4j
public class DDZCardRule {


    /**
     * 判断出牌和自己上一次储存的牌信息是否相同， 若相同则说明自己可以任意出牌(符合规则)， 否则，必须出比上家大的牌
     * @param x
     * @param y
     * @return
     */
    public static boolean isSame(String[] x, String[] y) {
        boolean result = true;
        if (x.length == y.length)
            for (int i = 0; i < x.length; i++) {
                if (!x[i].equals(y[i]))
                    result = false;
            }
        else
            result = false;
        return result;
    }

    /**
     * 出牌是否合法
     * @param cards
     * @return
     */
    public static DdzPokerType playValid(Integer[] cards) {
        DdzPokerType result = null;
        int length = cards.length;
        switch (length) {
            case 1:
                if (IsSingle(cards) != 0) {
                    result = DdzPokerType.SINGLE;
                }
                break;
            case 2:
                if (IsDouble(cards) != 0) {
                    result = DdzPokerType.TWIN;
                }
                if (IsJokerBoom(cards)!=0){
                    result = DdzPokerType.KING_BOMB;
                }
                break;
            case 3:
                if (IsOnlyThree(cards) != 0) {
                    result = DdzPokerType.TRIPLE;
                }
                break;
            case 4:
                if (IsBoom(cards) != 0) {
                    result = DdzPokerType.FOUR_BOMB;
                }
                if (IsThreeAndOne(cards)!=0){
                    result = DdzPokerType.TRIPLE_WITH_SINGLE;
                }
                break;
            case 5:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                if (IsThreeAndTwo(cards) != 0) {
                    result = DdzPokerType.TRIPLE_WITH_TWIN;
                }
                break;
            case 6:
                if (IsDoubleStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_TWIN;
                }
                if (IsTripleStraight(cards) != 0) {
                    result = DdzPokerType.PLANE_PURE;
                }
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                if (isSiDaiErSingle(cards)!=0){
                    result = DdzPokerType.FOUR_WITH_SINGLE;
                }
                if (isSiDaiErTwin(cards)!=0){
                    result = DdzPokerType.FOUR_WITH_TWIN;
                }
                break;
            case 7:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                break;
            case 8:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                if (IsDoubleStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_TWIN;
                }
                if (isPlaneWithSingle(cards) != 0) {
                    result = DdzPokerType.PLANE_WITH_SINGLE;
                }
                break;
            case 9:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                if (IsTripleStraight(cards) != 0) {
                    result = DdzPokerType.PLANE_PURE;
                }
                break;
            case 10:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                if (isPlaneWithTwin(cards) != 0) {
                    result = DdzPokerType.PLANE_WITH_TWIN;

                }
                if (IsDoubleStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_TWIN;
                }
                break;
            case 11:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                break;
            case 12:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                if (IsTripleStraight(cards) != 0) {
                    result = DdzPokerType.PLANE_PURE;
                }
                if (IsDoubleStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_TWIN;
                }
                break;
            case 13:
                if (IsStraight(cards) != 0) {
                    result = DdzPokerType.STRAIGHT_SINGLE;
                }
                break;
        }
        return result;
    }


    /**
     * 判断自己的牌是否大于上家的牌
     * @param myCards
     * @param myCardType
     * @param lastCards
     * @param lastCardTye
     * @return
     */
    public static boolean isSelectCardCanPut(Integer[] myCards,DdzPokerType myCardType, Integer[] lastCards, DdzPokerType lastCardTye)
    {
        // 我的牌和上家的牌都不能为null
        if (myCards == null || lastCards == null)
        {
            return false;
        }

        if (myCardType == null || lastCardTye == null)
        {
            return false;
        }

        // 上一首牌的个数
        int prevSize = lastCards.length;
        int mySize = myCards.length;

        // 我先出牌，上家没有牌
        if (prevSize == 0 && mySize != 0)
        {
            return true;
        }

        // 集中判断是否王炸，免得多次判断王炸
        if (lastCardTye == DdzPokerType.KING_BOMB)
        {
            log.debug("上家王炸，肯定不能出。");
            return false;
        }
        else if (myCardType == DdzPokerType.KING_BOMB)
        {
            log.debug("我王炸，肯定能出。");
            return true;
        }

        // 集中判断对方不是炸弹，我出炸弹的情况
        if (lastCardTye != DdzPokerType.FOUR_BOMB && myCardType == DdzPokerType.FOUR_BOMB)
        {
            return true;
        }

        //所有牌提前排序过了

        int myGrade = myCards[0];
        int prevGrade = lastCards[0];

        // 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
        // 2.我出炸弹，此时，和上家的牌的类型可能不同
        // 王炸的情况已经排除

        // 单
        if (lastCardTye == DdzPokerType.SINGLE && myCardType == DdzPokerType.SINGLE)
        {
            // 一张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);
        }
        // 对子
        else if (lastCardTye == DdzPokerType.TWIN&& myCardType == DdzPokerType.TWIN)
        {
            // 2张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);

        }
        // 3不带
        else if (lastCardTye == DdzPokerType.TRIPLE&& myCardType == DdzPokerType.TRIPLE)
        {
            // 3张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);
        }
        // 炸弹
        else if (lastCardTye == DdzPokerType.FOUR_BOMB&& myCardType == DdzPokerType.FOUR_BOMB)
        {
            // 4张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);

        }
        // 3带1
        else if (lastCardTye == DdzPokerType.TRIPLE_WITH_SINGLE&&myCardType == DdzPokerType.TRIPLE_WITH_SINGLE)
        {

            // 3带1只需比较第2张牌的大小
            myGrade = myCards[1];
            prevGrade = lastCards[1];
            return compareGrade(myGrade, prevGrade);

        }
        else if (lastCardTye == DdzPokerType.TRIPLE_WITH_TWIN&&myCardType == DdzPokerType.TRIPLE_WITH_TWIN)
        {

            // 3带2只需比较第3张牌的大小
            myGrade = myCards[2];
            prevGrade = lastCards[2];
            return compareGrade(myGrade, prevGrade);

        }

        // 4带2
        else if (lastCardTye == DdzPokerType.FOUR_WITH_SINGLE
                && myCardType == DdzPokerType.FOUR_WITH_SINGLE)
        {

            // 4带2只需比较第3张牌的大小
            myGrade = myCards[2];
            prevGrade = lastCards[2];
            return compareGrade(myGrade, prevGrade);

        }
        // 4带2对子
        else if (lastCardTye == DdzPokerType.FOUR_WITH_TWIN
                && myCardType == DdzPokerType.FOUR_WITH_TWIN)
        {
            myGrade = myCards[2];
            prevGrade = lastCards[2];
            return compareGrade(myGrade, prevGrade);
        }
        // 顺子
        else if (lastCardTye == DdzPokerType.STRAIGHT_SINGLE
                && myCardType == DdzPokerType.STRAIGHT_SINGLE)
        {
            if (mySize != prevSize)
            {
                return false;
            }
            else
            {
                // 顺子只需比较最大的1张牌的大小
                myGrade = myCards[mySize-1];
                prevGrade = lastCards[prevSize - 1];
                return compareGrade(myGrade, prevGrade);
            }

        }
        // 连对
        else if (lastCardTye == DdzPokerType.STRAIGHT_TWIN
                && myCardType == DdzPokerType.STRAIGHT_TWIN)
        {
            if (mySize != prevSize)
            {
                return false;
            }
            else
            {
                // 顺子只需比较最大的1张牌的大小
                myGrade = myCards[mySize - 1];
                prevGrade = lastCards[prevSize - 1];
                return compareGrade(myGrade, prevGrade);
            }

        }
        // 飞机不带
        else if (lastCardTye == DdzPokerType.PLANE_PURE
                && myCardType == DdzPokerType.PLANE_PURE)
        {
            if (mySize != prevSize)
            {
                return false;
            }
            else
            {
                //333444555666算飞机不带 不算飞机带单
                myGrade = myCards[4];
                prevGrade = lastCards[4];
                return compareGrade(myGrade, prevGrade);
            }
        }
        //飞机带单
        else if (lastCardTye == DdzPokerType.PLANE_WITH_SINGLE
                && myCardType == DdzPokerType.PLANE_WITH_SINGLE)
        {
            if (mySize != prevSize)
            {
                return false;
            }
            else
            {
                myGrade = isPlaneWithSingle(myCards);
                prevGrade = isPlaneWithSingle(lastCards);
                return compareGrade(myGrade, prevGrade);
            }
        }
        //飞机带双
        else if (lastCardTye == DdzPokerType.PLANE_WITH_TWIN
                && myCardType == DdzPokerType.PLANE_WITH_TWIN)
        {
            if (mySize != prevSize)
            {
                return false;
            }
            else
            {

                myGrade = isPlaneWithTwin(myCards);
                prevGrade = isPlaneWithTwin(lastCards);
                return compareGrade(myGrade, prevGrade);

            }
        }

        // 默认不能出牌
        return false;
    }

    private static boolean compareGrade(int myGrade, int prevGrade) {
        return myGrade>prevGrade?true:false;
    }


    /**
     * 是否是单张
     * @param cards
     * @return
     */
    public static int IsSingle(Integer[] cards)
    {
        if (cards.length == 1)
            return cards[0];
        else
            return 0;
    }

    /**
     * 是否对子
     * @param cards
     * @return
     */
    public static int IsDouble(Integer[] cards)
    {
        if (cards.length == 2)
        {
            if (cards[0] == cards[1])
                return cards[0];
        }

        return 0;
    }

    /**
     * 是否顺子
     * @param cards
     * @return
     */
    public static int IsStraight(Integer[] cards)
    {
        Arrays.sort(cards);
        if (cards.length < 5 || cards.length > 12)
            return 0;
        for (int i = 0; i < cards.length - 1; i++)
        {
            int w = cards[i];
            if (cards[i + 1] - w != 1)
                return 0;

            //不能超过A
            if (w > 12 || cards[i + 1]> 12)
                return 0;
        }

        return cards[0];
    }

    /**
     * 是否双顺子
     * @param cards
     * @return
     */
    public static int IsDoubleStraight(Integer[] cards)
    {
        Arrays.sort(cards);
        if (cards.length < 6 || cards.length % 2 != 0)
            return 0;

        for (int i = 0; i < cards.length; i += 2)
        {
            if (cards[i + 1] != cards[i])
                return 0;

            if (i < cards.length - 2)
            {
                if (cards[i + 2] - cards[i] != 1)
                    return 0;

                //不能超过A
                if (cards[i] > 12 || cards[i + 2] > 12)
                    return 0;
            }
        }

        return cards[0];
    }


    /**
     * 是否三不带
     * @param cards
     * @return
     */
    public static int IsOnlyThree(Integer[] cards)
    {
        if (cards.length !=3)
            return 0;
        if (cards[0]==cards[1]&&cards[1]==cards[2]){
            return cards[0];
        }
        return 0;
    }


    /**
     * 是否三带一
     * @param cards
     * @return
     */
    public static int IsThreeAndOne(Integer[] cards)
    {
        Arrays.sort(cards);
        if (cards.length != 4) {
            return 0;
        }
        if (cards[0] == cards[1] &&
                cards[1] == cards[2]) {
            return cards[0];
        }else if (cards[1] == cards[2] &&
                cards[2] == cards[3]) {
            return cards[1];
        }else {
            return 0;
        }
    }

    /**
     * 是否三带二
     * @param cards
     * @return
     */
    public static int IsThreeAndTwo(Integer[] cards)
    {
        Arrays.sort(cards);
        if (cards.length != 5) {
            return 0;
        }
        if (cards[0] == cards[1] &&
                cards[1] == cards[2])
        {
            if (cards[3] == cards[4])
                return cards[0];
        }

        else if (cards[2] == cards[3] &&
                cards[3] == cards[4])
        {
            if (cards[0] == cards[1])
                return cards[2];
        }
        return 0;

    }

    /**
     * 是否炸弹
     * @param cards
     * @return
     */
    public static int IsBoom(Integer[] cards)
    {
        if (cards.length != 4)
            return 0;

        if (cards[0] != cards[1]&&cards[1] != cards[2]&&cards[2] != cards[3]) {
            return cards[0];
        }else {
            return 0;
        }
    }


    /**
     * 是否王炸
     * @param cards
     * @return
     */
    public static int IsJokerBoom(Integer[] cards)
    {
        Arrays.sort(cards);
        if (cards.length != 2)
            return 0;
        if (cards[0] == 53&&cards[1] == 54)
        {
            return cards[0];
        }

        return 0;
    }

    /**
     * 是否飞机不带
     * @param cards
     * @return
     */
    public static int IsTripleStraight(Integer[] cards)
    {
        Arrays.sort(cards);
        if (cards.length < 6 || cards.length % 3 != 0)
            return 0;

        for (int i = 0; i < cards.length; i += 3)
        {
            if (cards[i + 1] != cards[i])
                return 0;
            if (cards[i + 2] != cards[i])
                return 0;
            if (cards[i + 1] != cards[i + 2])
                return 0;

            if (i < cards.length - 3)
            {
                if (cards[i + 3] - cards[i] != 1)
                    return 0;

                //不能超过A
                if (cards[i] > 12 || cards[i + 3] > 12)
                    return 0;
            }
        }

        return cards[0];
    }

    /**
     * 飞机带单
     * @param cards
     * @return
     */
    public static int isPlaneWithSingle(Integer[] cards)
    {
        if (cards.length >= 4&&cards.length % 4 == 0&&!HaveFour(cards)) {
            List<Integer> tempThreeList = new ArrayList<>();
            for (int i = 0; i < cards.length; i++)
            {
                int tempInt = 0;
                for (int j = 0; j < cards.length; j++)
                {

                    if (cards[i] == cards[j])
                    {
                        tempInt++;
                    }

                }
                if (tempInt == 3)
                {
                    tempThreeList.add(cards[i]);
                }
            }
            if (tempThreeList.size() % 3 != 0){
                return 0;
            }else {
                int straight = IsTripleStraight(tempThreeList.toArray(new Integer[tempThreeList.size()]));
                if (straight>0)
                {
                    return straight;
                }
                else {
                    return 0;
                }
            }
        }

        return 0;
    }

    /**
     * 飞机带双
     * @param cards
     * @return
     */
    public static int isPlaneWithTwin(Integer[] cards)
    {
        if (cards.length >= 5&&cards.length % 5 == 0&&!HaveFour(cards))
        {
            List<Integer> tempThreeList = new ArrayList<>();
            List<Integer> tempTwoList = new ArrayList<>();
            for (int i = 0; i < cards.length; i++)
            {
                int tempInt = 0;
                for (int j = 0; j < cards.length; j++)
                {

                    if (cards[i] == cards[j])
                    {
                        tempInt++;
                    }

                }
                if (tempInt == 3)
                {
                    tempThreeList.add(cards[i]);
                }
                else if (tempInt==2) {
                    tempTwoList.add(cards[i]);
                }

            }
            if (tempThreeList.size() % 3 != 0 && tempTwoList.size()%2!= 0)
            {

                return 0;
            }
            else
            {
                int straight = IsTripleStraight(tempThreeList.toArray(new Integer[tempThreeList.size()]));
                if (straight>0)
                {
                    if (IsAllDouble(tempTwoList.toArray(new Integer[tempTwoList.size()])))
                    {
                        return straight;
                    }
                    else {
                        return 0;
                    }
                }

            }
        }
        return 0;
    }

    /**
     * 判断是否有四张牌
     * @param cards
     * @return
     */
    public static boolean HaveFour(Integer[] cards) {

        for (int i = 0; i < cards.length; i++)
        {
            int tempInt = 0;
            for (int j = 0; j < cards.length; j++)
            {

                if (cards[i] == cards[j])
                {
                    tempInt++;
                }
            }
            if (tempInt == 4)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断全是对子
     * @param cards
     * @return
     */
    public static boolean IsAllDouble(Integer[] cards)
    {
        for (int i = 0; i < cards.length % 2; i += 2) {
            if (cards[i] != cards[i + 1]) {
                return false;
            }
        }
        return true;
    }


    /**
     * 是否四带二单
     * @param cards
     * @return
     */
    public static int isSiDaiErSingle(Integer[] cards)
    {
        if (cards != null && cards.length== 6)
        {
            Arrays.sort(cards);
            if (cards[0] == cards[1]&&cards[0] == cards[2]&&cards[0] == cards[3]){
                if (cards[4]!=cards[5]) {
                    return cards[0];
                }
            }else if (cards[1] == cards[2]&&cards[1] == cards[3]&&cards[1] == cards[4]){
                return cards[1];
            }else if (cards[2] == cards[3]&&cards[2] == cards[4]&&cards[2] == cards[5]){
                if (cards[0]!=cards[1]) {
                    return cards[2];
                }
            }
        }
        return 0;
    }

    /**
     * 是否四带二双
     * @param cards
     * @return
     */
    public static int isSiDaiErTwin(Integer[] cards)
    {
        if (cards != null && cards.length== 8)
        {
            Arrays.sort(cards);
            if (cards[0] == cards[1]&&cards[0] == cards[2]&&cards[0] == cards[3]){
                if (cards[4]==cards[5]&&cards[6]==cards[7]) {
                    return cards[0];
                }

            }else if (cards[2] == cards[3]&&cards[2] == cards[4]&&cards[2] == cards[5]){
                if (cards[0]==cards[1]&&cards[6]==cards[7]) {
                    return cards[2];
                }
            }else if (cards[4] == cards[5]&&cards[4] == cards[6]&&cards[4] == cards[7]){
                if (cards[0]==cards[1]&&cards[2]==cards[3]) {
                    return cards[4];
                }
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        Map<Integer, String> pooker = DdzStaticData.pooker;
        System.out.println(pooker.size());
    }


}
