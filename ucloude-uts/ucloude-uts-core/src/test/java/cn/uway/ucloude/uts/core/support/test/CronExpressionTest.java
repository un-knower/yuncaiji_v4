package cn.uway.ucloude.uts.core.support.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import cn.uway.ucloude.uts.core.support.CronExpression;

public class CronExpressionTest {
	@Test
    public void test1() throws ParseException {

//        CronExpression cronExpression = new CronExpression("59 23 * * *");
//
//        exec(cronExpression, new Date());
		System.out.println("xxxxxx");
    }

    private Date exec(CronExpression cronExpression, Date date){
    	return null;
//        Date nextDate = cronExpression.getTimeAfter(date);
//
//        if(nextDate != null){
//            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextDate));
//
//            nextDate.setTime(nextDate.getTime() + 100);
//            exec(cronExpression, nextDate);
//        }else{
//            System.out.println("执行完成");
//        }
//
//        return nextDate;
    }
}
