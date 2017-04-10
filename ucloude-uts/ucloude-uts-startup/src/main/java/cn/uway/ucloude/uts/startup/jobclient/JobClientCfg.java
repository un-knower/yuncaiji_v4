package cn.uway.ucloude.uts.startup.jobclient;

import java.util.Map;

public class JobClientCfg {

    private String[] springXmlPaths;
	public String[] getSpringXmlPaths() {
		return springXmlPaths;
	}
	public void setSpringXmlPaths(String[] springXmlPaths) {
		this.springXmlPaths = springXmlPaths;
	}
	private String nodeGroup;
	  private Map<String, String> configs;
	    public Map<String, String> getConfigs() {
		return configs;
	}
	public void setConfigs(Map<String, String> configs) {
		this.configs = configs;
	}
		public String getNodeGroup() {
		return nodeGroup;
	}
	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
	public boolean isUseRetryClient() {
		return useRetryClient;
	}
	public void setUseRetryClient(boolean useRetryClient) {
		this.useRetryClient = useRetryClient;
	}
	public String getDataPath() {
		return dataPath;
	}
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
		private boolean useRetryClient = true;
	    private String dataPath;
	    
	    private String registryAddress;
		public String getRegistryAddress() {
			return registryAddress;
		}
		public void setRegistryAddress(String registryAddress) {
			this.registryAddress = registryAddress;
		}
		
		private String clusterName;
		public String getClusterName() {
			return clusterName;
		}
		public void setClusterName(String clusterName) {
			this.clusterName = clusterName;
		}
}
