package cn.uway.ucloude.serialize;

import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.StringUtil;

public class JsonFactory {
	private static final ILogger LOGGER = LoggerManager.getLogger(JsonFactory.class);

    private static volatile JsonAdapter JSON_ADAPTER;

    static {
        String json = System.getProperty("ucloude.json");
        if(StringUtil.isEmpty(json))
        	json = "fastjson";
        setJSONAdapter(ServiceFactory.load(JsonAdapter.class,json));
    }

    public static void setJSONAdapter(String jsonAdapter) {
        if (StringUtil.isNotEmpty(jsonAdapter)) {
            setJSONAdapter(ServiceFactory.load(JsonAdapter.class, jsonAdapter));
        }
    }

    public static JsonAdapter getJSONAdapter() {
        return JsonFactory.JSON_ADAPTER;
    }

    public static void setJSONAdapter(JsonAdapter jsonAdapter) {
        if (jsonAdapter != null) {
            LOGGER.info("Using JSON lib " + jsonAdapter.getName());
            JsonFactory.JSON_ADAPTER = jsonAdapter;
        }
    }
}
