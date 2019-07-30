package com.secusoft.web.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 定时任务管理类
 * Created by wzz on 2019/7/17
 */
public class QuartzCronDateUtil {
    /***
     *  功能描述：日期转换cron表达式时间格式
     * @param date
     * @param dateFormat : e.g:yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String formatDateByPattern(Date date, String dateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String formatTimeStr = null;
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }
    /***
     * convert Date to cron ,eg.  "14 01 17 22 07 ? 2017"
     * @param date:时间点
     * @return
     */
    public static String getDateCron(Date  date){
        String dateFormat="ss mm HH dd MM ?";
        return formatDateByPattern(date,dateFormat);
    }
    public static String getTimeCron(Date  date){
        String dateFormat="ss mm HH";
        return formatDateByPattern(date,dateFormat);
    }
    public static String getTemplateCron(){
        return null;
    }
}