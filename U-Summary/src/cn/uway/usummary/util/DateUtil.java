package cn.uway.usummary.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	// 解决DateFormat线程不安全问题
	private static ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {  
		  
        @Override  
        protected DateFormat initialValue() {  
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        }  
    }; 
	
	public static Date parseDate(String date) throws ParseException{
		return df.get().parse(date);
	}
	
	public static String getCurrentTime(){
		return df.get().format(new Date());
	}
	
	public static String getCurrentHour(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getCurrentDay(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getPastTime(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, -1);
		return df.get().format(new Date(cal.getTimeInMillis()));
	}

	public static String getPastHour(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getPastDay(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.add(Calendar.HOUR_OF_DAY, 0);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getCurrentWeek(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		// 星期天
		if(cal.get(Calendar.DAY_OF_WEEK) == 1)
		{
			cal.add(Calendar.DAY_OF_MONTH,-6);
		}
		// 星期一到六
		else
		{
			cal.add(Calendar.DAY_OF_MONTH,-(cal.get(Calendar.DAY_OF_WEEK)-2));
		}	
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getPastWeek(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.add(Calendar.DAY_OF_MONTH, -7);
		// 星期天
		if(cal.get(Calendar.DAY_OF_WEEK) == 1)
		{
			cal.add(Calendar.DAY_OF_MONTH,-6);
		}
		// 星期一到六
		else
		{
			cal.add(Calendar.DAY_OF_MONTH,-(cal.get(Calendar.DAY_OF_WEEK)-2));
		}	
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getCurrentMonth(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getPastMonth(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, -1);
		return df.get().format(new Date(cal.getTimeInMillis()));
	}
	
	public static String getYear(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return String.valueOf(cal.get(Calendar.YEAR));
	}

	public static String getMonth(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return String.valueOf(cal.get(Calendar.MONTH)+1);
	}

	public static String getDay(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	}
	
	public static String getYesterday(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	}
	
	public static String getHour(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	}
}
