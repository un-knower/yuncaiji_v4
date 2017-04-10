package cn.uway.ucloude.uts.web.admin.support;


import org.springframework.util.StringUtils;

import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.serialize.TypeReference;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * magic.s.g.xie
 */
public class MapEditor extends PropertyEditorSupport {

    public MapEditor() {
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            setValue(null);
        } else {
            setValue(JsonConvert.deserialize(text, new TypeReference<HashMap<String, String>>(){}));
        }
    }

    @Override
    public String getAsText() {
        Map<?, ?> value = (Map<?, ?>) getValue();

        if (value == null) {
            return "";
        }
        return JsonConvert.serialize(value);
    }
}
