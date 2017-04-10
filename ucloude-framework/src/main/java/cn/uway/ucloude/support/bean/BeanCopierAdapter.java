package cn.uway.ucloude.support.bean;

/**
 * @author uway
 */
public abstract class BeanCopierAdapter implements BeanCopier<Object, Object> {

    public abstract void copyProps(Object sourceObj, Object targetObj);
}
