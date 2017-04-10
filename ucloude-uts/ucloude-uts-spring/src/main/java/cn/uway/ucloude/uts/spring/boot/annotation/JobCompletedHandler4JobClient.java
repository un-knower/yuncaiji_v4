package cn.uway.ucloude.uts.spring.boot.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于JobClient的任务完成处理器
 * @author magic.s.g.xie
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JobCompletedHandler4JobClient {

}
