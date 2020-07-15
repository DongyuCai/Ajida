package com.ajida;

import java.util.HashMap;
import java.util.Map;

public class AxeAppConfig {

	/**
	 * 配置参数，用以{{xxx}}替换，所有的拷贝文件都适用
	 */
	private Map<String,String> configParams = new HashMap<>();
	
	/**
	 * 应用启动的Main类，以及可以携带启动参数
	 */
	private String applicationMainClassAndStartParams;
	
	private int index = 0;//配置的所在顺序，对应启动节点

	public AxeAppConfig(String applicationMainClassAndStartParams) {
		this.applicationMainClassAndStartParams = applicationMainClassAndStartParams;
	}

	public Map<String, String> getConfigParams() {
		return configParams;
	}
	
	public void addConfigParams(String name,String value){
		configParams.put(name, value);
	}

	public String getApplicationMainClassAndStartParams() {
		return applicationMainClassAndStartParams;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
