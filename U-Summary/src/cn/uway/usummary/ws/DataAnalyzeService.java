package cn.uway.usummary.ws;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface DataAnalyzeService {
	
	public String DataAnalyze(@WebParam(name = "sqlNum")String sqlNum, @WebParam(name = "placeholder") String placeholder);
	
}
