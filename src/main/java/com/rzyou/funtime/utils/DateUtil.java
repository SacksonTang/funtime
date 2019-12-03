package com.rzyou.funtime.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtil {
    public final static String YYYY_MM_DD_HH_MM_SS_TIGHT = "yyyyMMddHHmmss";
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String YYYYMMDD = "yyyyMMdd";
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static SimpleDateFormat SDF1 = new SimpleDateFormat(YYYY_MM_DD, Locale.CHINA);
    public static SimpleDateFormat SDF2 = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_TIGHT, Locale.CHINA);
    public static SimpleDateFormat SDF3 = new SimpleDateFormat(YYYYMMDD, Locale.CHINA);


    private final static int[] dayArr = new int[] { 20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22 };
    private final static String[] constellationArr = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };
    public static String getConstellation(int month, int day) {
        return day < dayArr[month - 1] ? constellationArr[month - 1] : constellationArr[month];
    }

    public static String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_TIGHT, Locale.CHINA);

        String time = df.format(date);
        return time;
    }
    public static String getCurrentDateTimeExtr() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS, Locale.CHINA);

        String time = df.format(date);
        return time;
    }

    public static int getAgeByBirthday(int birthday){
        int currentInt = getCurrentInt();
        int age;
        if ((currentInt-birthday)%10000>0){
            age = (currentInt-birthday)/10000+1;
        }else{
            age = (currentInt-birthday)/10000;
        }
        return age;
    }

    public static String getConstellationByBirthday(int birthday){
        String birth = String.valueOf(birthday);
        int month = Integer.valueOf(birth.substring(4,6));
        int day = Integer.valueOf(birth.substring(6,8));
        return getConstellation(month,day);
    }

    public static String getCurrentDateTime(String format) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.CHINA);

        String time = df.format(date);
        return time;
    }


    public static Date getDateByDayMonth(String day_month) throws Exception{
        String date = day_month+"-01";
        return SDF1.parse(date);
    }

    public static int getCurrentInt(){
        String current = SDF3.format(new Date());
        return Integer.parseInt(current);
    }

    /**
     * 当前时间N年
     */
    public static String getCurrentYearAdd(Date date,Integer n){
        Date addYears = DateUtils.addYears(date, n);
        return SDF3.format(addYears);
    }

    /**
     * 获取当天时间的开始
     */
    public static String getCurrentDayStart(){
        String date = getCurrentDateTime(YYYY_MM_DD);
        return date+" 00:00:00";
    }

    /**
     * 获取当天时间的结束
     */
    public static String getCurrentDayEnd(){
        String date = getCurrentDateTime(YYYY_MM_DD);
        return date+" 23:59:59";
    }

    /**
     *
     * @Description：查询月份最后时间
     *
     */
    public static String currentFinalDay(String date) throws Exception{

        Calendar calendar=Calendar.getInstance();
        calendar.setTime(getDateByDayMonth(date));
        //设置日期为本月最大日期
        calendar.set(Calendar.DATE, calendar.getActualMaximum(calendar.DATE));
        //设置日期格式
        return SDF1.format(calendar.getTime())+" 23:59:59";
    }

    public static void main(String[] args) throws Exception{
        System.out.println(currentFinalDay("2018-11"));
    }

}
