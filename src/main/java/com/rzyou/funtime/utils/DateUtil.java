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

    /**
     *
     * @Description：查询当月开始时间
     * @Date：2018/11/22 下午2:54
     * @Author：ChengJian
     * @UpdateRemark:
     * @Version:1.0
     *
     */
    public static String currentFirstDay(Date date){
        Calendar calendar=Calendar.getInstance();
        Date theDate=calendar.getTime();
        GregorianCalendar gcLast=(GregorianCalendar)Calendar.getInstance();
        gcLast.setTime(date);
        //设置为第一天
        gcLast.set(Calendar.DAY_OF_MONTH, 1);
        return SDF1.format(gcLast.getTime())+" 00:00:00";
    }

    /**
     *
     * @Description：查询当月最后时间
     * @Date：2018/11/22 下午2:58
     * @Author：ChengJian
     * @UpdateRemark:
     * @Version:1.0
     *
     */
    public static String currentFinalDay(Date date){
        Calendar calendar=Calendar.getInstance();
        //设置日期为本月最大日期
        calendar.set(Calendar.DATE, calendar.getActualMaximum(calendar.DATE));
        //设置日期格式
        return SDF1.format(calendar.getTime())+" 23:59:59";
    }

}
