package cn.uway.ucloude.uts.spring.boot.annotation;


import org.springframework.stereotype.Component;

import cn.uway.ucloude.uts.core.cluster.NodeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Robert magic.s.g.xie
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface MasterNodeListener {

    NodeType[] nodeTypes() default {};

}
