package cn.uway.ucloude.uts.spring.tasktracker;

import java.lang.annotation.*;

/**
 * @author magic.s.g.xie
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface JobRunnerItem {

    String shardValue() default "";
}
