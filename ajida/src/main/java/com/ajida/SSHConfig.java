package com.ajida;

public class SSHConfig {
	
	private String ip;
	private String name;
	private String password;
	private int port = 22;//默认22端口
	
	public SSHConfig(String ip, String name, String password) {
		this.ip = ip;
		this.name = name;
		this.password = password;
	}
	
	public SSHConfig(String ip, String name, String password, int port) {
		this.ip = ip;
		this.name = name;
		this.password = password;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}
}
