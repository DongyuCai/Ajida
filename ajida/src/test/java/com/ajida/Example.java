package com.ajida;

import java.util.Scanner;

import com.ajida.util.SSHConfig;

public class Example {
	public static void main(String[] args) {
		try {
			xjp_45();
//			xjp_114();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void xjp_45() throws Exception{
		System.out.println("enter password:");
		Scanner sc = new Scanner(System.in);
		String password = sc.nextLine();
		sc.close();
		
		SSHConfig sshConfig = new SSHConfig("192.168.199.45", "root", password);
		
		Ajida.stopTomcat(sshConfig);
		
		//xjp-sdk
		Ajida.javaSdkInstall("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-sdk");
		
		//xjp-admin
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin\\config\\test",
				"/usr/local/apache-tomcat-9.0.12",
				sshConfig);
		
		//xjp-collector
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\config\\test",
				"/usr/local/apache-tomcat-9.0.12",
				sshConfig);
		
		//xjp-user
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\test",
				"/usr/local/apache-tomcat-9.0.12",
				sshConfig);
		
		Ajida.startTomcat("/usr/local/apache-tomcat-9.0.12",sshConfig);
		
		/*
		Ajida.htmlProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_test",
				"/usr/nginx/html", 
				sshConfig);*/
	}
	
	public static void xjp_114() throws Exception{
		System.out.println("enter password:");
		Scanner sc = new Scanner(System.in);
		String password = sc.nextLine();
		sc.close();
		
		SSHConfig sshConfig = new SSHConfig("114.218.158.239", "root", password);
		Ajida.stopTomcat(sshConfig);
		
		//xjp-sdk
		Ajida.javaSdkInstall("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-sdk");
		
		//xjp-admin
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin\\config\\pro",
				"/usr/local/apache-tomcat-9.0.13",
				sshConfig);

		//xjp-collector
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\config\\pro",
				"/usr/local/apache-tomcat-9.0.13",
				sshConfig);
		


		//xjp-collector
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\config\\pro",
				"/usr/local/apache-tomcat-9.0.13",
				sshConfig);
		
		//xjp-user
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\pro",
				"/usr/local/apache-tomcat-9.0.13",
				sshConfig);
		
		Ajida.startTomcat("/usr/local/apache-tomcat-9.0.13",sshConfig);
		/*
		Ajida.htmlProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_pro",
				"/var/html", 
				sshConfig);
		*/
//		Ajida.update(resourceDir, targetDir, project, linuxIp, linuxUsername, linuxPassword, tomcatDir, restartTomcat);
	}

}
