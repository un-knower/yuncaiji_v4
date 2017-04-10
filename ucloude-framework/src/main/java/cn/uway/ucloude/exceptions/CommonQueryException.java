package cn.uway.ucloude.exceptions;

/**
 *查询异常
 * @author uway
 *
 */
public class CommonQueryException extends Exception{

	/** 唯一标识*/
	private static final long serialVersionUID = 7062021700515834441L;

	/** 查询ID */
	protected int queryId;

	/** 数据库提交命令 */
	protected String statement;

	/**
	 * 构造函数
	 */
	public CommonQueryException(){
		super();
	}


	public CommonQueryException(String message, Throwable cause, int queryId, String statement){
		super(message, cause);
	}

	
	public CommonQueryException(String message, Throwable cause){
		super(message, cause);
	}


	public CommonQueryException(String message){
		super(message);
	}

	public CommonQueryException(Throwable cause){
		super(cause);
	}

	public int getQueryId(){
		return queryId;
	}


	public String getStatement(){
		return statement;
	}

}
