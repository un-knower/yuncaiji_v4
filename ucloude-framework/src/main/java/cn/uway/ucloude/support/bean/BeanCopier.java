package cn.uway.ucloude.support.bean;

/**
 * @author uway
 */
public interface BeanCopier<Source, Target> {

    /**
     * 拷贝属性
     */
    void copyProps(Source source, Target target);

}
