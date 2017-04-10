package cn.uway.ucloude.data.dataaccess.builder;

import org.apache.commons.lang3.StringUtils;

/**
 * @author magic.s.g.xie
 */
public enum OrderByType {
    DESC, ASC;

    public static OrderByType convert(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return OrderByType.valueOf(value);
    }

}
