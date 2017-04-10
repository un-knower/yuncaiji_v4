package cn.uway.ucloude.uts.web.admin.vo;

import java.io.Serializable;
import java.util.List;

public class RestfulResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4392531418947646324L;
	private boolean success;

    private String code;

    private String msg;
    /**
     * total recorded
     */
    private int results;
    /**
     * rows
     */
    private List<?> rows;
    
    private Object data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
    
}
