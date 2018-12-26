package com.ajida;

public class Example {
	public static void main(String[] args) {
		try {
			xjp_user_45(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void xjp_user_45(boolean restartTomcat) throws Exception{
		//配置文件
		String resourceDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\test";
		String targetDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\target";
		String project = "\\xjp-user";
		//远程账号
		String linuxIp = "192.168.199.45";
		String linuxUsername = "root";
		String linuxPassword = "ybsl1234";
		String tomcatDir = "/usr/local/apache-tomcat-9.0.12";
		
		Ajida.update(resourceDir, targetDir, project, linuxIp, linuxUsername, linuxPassword, tomcatDir, restartTomcat);
	}

}
