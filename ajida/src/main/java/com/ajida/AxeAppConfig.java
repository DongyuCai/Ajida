package com.ajida;

import java.util.HashMap;
import java.util.Map;

public class AxeAppConfig {

	/**
	 * nginx配置参数
	 */
	private Map<String,String> nginxParams = new HashMap<>();
	
	/**
	 * 应用启动的Main类，以及可以携带启动参数
	 */
	private String applicationMainClassAndStartParams;
	
	private int index = 0;//配置的所在顺序，对应启动节点

	public AxeAppConfig(String applicationMainClassAndStartParams) {
		this.applicationMainClassAndStartParams = applicationMainClassAndStartParams;
	}

	public Map<String, String> getNginxParams() {
		return nginxParams;
	}
	
	public void addNginxParams(String name,String value){
		nginxParams.put(name, value);
	}

	public String getApplicationMainClassAndStartParams() {
		return applicationMainClassAndStartParams;
	}

	public int getIndex() {
		return index;
	}

	protected void setIndex(int index) {
		this.index = index;
	}
	
}
