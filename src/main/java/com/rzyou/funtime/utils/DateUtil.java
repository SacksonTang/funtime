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

    public static SimpleDateFormat SDF1 = new SimpleDateFormat(YYYY_MM_DD, Locale.CHINA);

    public static String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_TIGHT, Locale.CHINA);

        String time = df.format(date);
        return time;
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
