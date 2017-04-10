package cn.uway.ucloude.support.bean;

public interface PropConverter<Source, Output> {

    /**
     * @param source 是原对象
     * @return 这个属性的值
     */
    Output convert(Source source);
}