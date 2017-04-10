package cn.uway.ucloude.uts.core.support;

import cn.uway.ucloude.utils.StringUtil;

public class ConfigValidator {
	
	public static void validateNodeGroup(String nodeGroup){
		if(StringUtil.isEmpty(nodeGroup)){
			throw new IllegalArgumentException("nodeGroup should not be null");
		}
		if (nodeGroup.length() > 64) {
            throw new IllegalArgumentException("nodeGroup length should not great than 64");
        }
	}
	
	public static void validateClusterName(String clusterName) {
        if (StringUtil.isEmpty(clusterName)) {
            throw new IllegalArgumentException("clusterName should not be null");
        }
        if (clusterName.length() > 64) {
            throw new IllegalArgumentException("clusterName length should not great than 64");
        }
    }

    public static void validateIdentity(String identity) {
        if (StringUtil.isNotEmpty(identity)) {
            if (identity.length() > 64) {
                throw new IllegalArgumentException("identity length should not great than 64");
            }
        }
    }
}
