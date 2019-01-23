package com.ajida;

import com.ajida.util.SSHConfig;

public class Example {
	public static void main(String[] args) {
		try {
			xjp_user_45();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void xjp_user_45() throws Exception{
		SSHConfig sshConfig = new SSHConfig("192.168.199.45", "root", "ybsl1234");
		Ajida.stopTomcat(sshConfig);
		
		
		/*Ajida.codeUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\test",
				"/usr/local/apache-tomcat-9.0.12",
				sshConfig);*/
		
		Ajida.startTomcat("/usr/local/apache-tomcat-9.0.12",sshConfig);
		
//		Ajida.update(resourceDir, targetDir, project, linuxIp, linuxUsername, linuxPassword, tomcatDir, restartTomcat);
	}

}
