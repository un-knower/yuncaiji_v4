package cn.uway.ucloude.uts.web.admin.support;

import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;

public class Builder {
	public static RestfulResponse build(boolean success, String msg) {
		RestfulResponse response = new RestfulResponse();
		response.setSuccess(success);
		response.setMsg(msg);
		return response;
	}

	public static RestfulResponse build(boolean success) {
		RestfulResponse response = new RestfulResponse();
		response.setSuccess(success);
		return response;
	}
}
