package cn.uway.ucloude.configuration.auto.annotation;
import java.lang.annotation.*;

/**
 * 和SpringBoot的类似
 * @author uway
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {
	  /**
     * 前缀
     */
    String prefix() default "";

    /**
     * 直接指定文件位置
     */
    String[] locations() default {};
}
