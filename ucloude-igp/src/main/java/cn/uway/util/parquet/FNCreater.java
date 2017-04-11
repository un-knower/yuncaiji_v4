package cn.uway.util.parquet;


/**
 * 各类文件有不同的文件命名规则
 * 
 * @author sunt
 *
 */
public abstract class FNCreater {

	/**
	 * 返回绝对路径的文件名
	 * 
	 * @return
	 */
	public abstract String getNewName();

}
