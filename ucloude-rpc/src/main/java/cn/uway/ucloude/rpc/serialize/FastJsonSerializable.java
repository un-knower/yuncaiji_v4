package cn.uway.ucloude.rpc.serialize;

import java.nio.charset.Charset;

import cn.uway.ucloude.serialize.JsonConvert;

public class FastJsonSerializable implements RpcSerializable {

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public byte[] serialize(Object obj) throws Exception {
		// TODO Auto-generated method stub
		 String json = toJson(obj, false);
	     return json.getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
		// TODO Auto-generated method stub
		 final String json = new String(data, Charset.forName("UTF-8"));
	     return fromJson(json, clazz);
	}

	 private String toJson(final Object obj, boolean prettyFormat) {
	        return JsonConvert.serialize(obj);
    }

    private <T> T fromJson(String json, Class<T> classOfT) {
        return JsonConvert.deserialize(json, classOfT);
    }
}
