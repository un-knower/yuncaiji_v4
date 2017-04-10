package cn.uway.ucloude.configuration;

public class UCloudeConfiguration {
	
	ConfigManager __inner = null;
	
	private UCloudeConfiguration(String file) throws Exception{
		__inner = ConfigManager.create(file);
	}
	
	/**
	 * 初始化配置文件
	 * @param file 配置文件
	 * @throws Exception 
	 */
    public static UCloudeConfiguration  setup(String file) throws Exception
    {
        return new UCloudeConfiguration(file);
    }

	
	public String get(String key){
		return __inner.getConfiguration().getString(key);
	}
}
