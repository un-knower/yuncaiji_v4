package cn.uway.ucloude.exceptions;

/**
 * 数据访问异常类。
 * @author uway
 *
 */
public class UCloudeConnectionException extends Exception{


	/** 序列化ID号。 */
	private static final long serialVersionUID = 151731790606562035L;

	/**
	 * 构造方法。
	 */
	public UCloudeConnectionException(){
		super();
	}

	/**
	 * 构造方法。
	 * 
	 * @param message 异常消息。
	 * @param cause 异常原因（上级异常）。
	 * @param queryId 异常所涉及到的查询ID（如果不存在，传入0）。
	 * @param statement 异常所涉及到的查询语句（如果不存在，传入<code>null</code>）。
	 */
	public UCloudeConnectionException(String message, Throwable cause, int queryId, String statement){
		super(message, cause);
	}

	/**
	 * 构造方法。
	 * 
	 * @param message 异常消息。
	 * @param cause 异常原因（上级异常）。
	 */
	public UCloudeConnectionException(String message, Throwable cause){
		super(message, cause);
	}

	/**
	 * 构造方法。
	 * 
	 * @param message 异常消息。
	 */
	public UCloudeConnectionException(String message){
		super(message);
	}

	/**
	 * 构造方法。
	 * 
	 * @param cause 异常原因（上级异常）。
	 */
	public UCloudeConnectionException(Throwable cause){
		super(cause);
	}

}
