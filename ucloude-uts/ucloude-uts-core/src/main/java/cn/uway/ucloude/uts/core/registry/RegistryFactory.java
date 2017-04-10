package cn.uway.ucloude.uts.core.registry;

import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.registry.zookeeper.ZookeeperRegistry;

public class RegistryFactory {
	public static Registry getRegistry(UtsContext appContext) {

        String address = appContext.getConfiguration().getRegistryAddress();
        if (StringUtil.isEmpty(address)) {
            throw new IllegalArgumentException("address is null！");
        }
        
        if (address.startsWith("zookeeper://")) {
            return new ZookeeperRegistry(appContext);
        } 
        
        throw new IllegalArgumentException("illegal address protocol");
    }
}
