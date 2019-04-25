package com.ajida;

import java.io.File;
import java.util.Scanner;

import org.axe.util.FileUtil;
import org.axe.util.StringUtil;

import com.ajida.util.CmdUtil;
import com.ajida.util.Logger;
import com.ajida.util.SSHConfig;
import com.ajida.util.SSHUtil;
import com.ajida.util.ZipUtil;

public class Example {
	public static void main(String[] args) {
		try {
			xjp_45();
//			xjp_114();
//			xjp_114_hot();

//			xjp_user_45();
//			xjp_user_114();
			/*
			for(int i=1;i<1000;i++){
				int result = (i+2)%2;
				
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void xjp_user_45(){
		String projectDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-user";
		String configDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-user\\xjp-user\\build\\js\\conf_test";
		
		String[] cmds = null;
		String projectName = projectDir.substring(projectDir.lastIndexOf("\\")+1);
		try {
			//1.清理
			Logger.log(">>> 1. clean folder");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"rd /s /q "+projectName,
					"del /f /s /q "+projectName+".zip"
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
			//2.fis 打包
			Logger.log(">>> 2. fis relase");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"fis3 release build -d ./"+projectName+"/build"
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
			//3.拷贝配置文件
			Logger.log(">>> 3. copy config files");
			String[] resourceFileList = new File(configDir).list();
			for(String rf:resourceFileList){
				FileUtil.copy(configDir+"\\"+rf, configDir+"\\..");
				Logger.log("copy:"+configDir+"\\"+rf);
			}
			
			//4.压缩打包
			Logger.log(">>> 4. compress files to zip");
			ZipUtil.compressDir(new File(projectDir+"\\"+projectName), projectDir+"\\"+projectName+".zip");

			//5.上传
			
			
			//6.录入新版本信息
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void xjp_user_114(){
		String projectDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-user";
		String configDir = "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-user\\xjp-user\\build\\js\\conf_pro";
		
		String[] cmds = null;
		String projectName = projectDir.substring(projectDir.lastIndexOf("\\")+1);
		try {
			//1.清理
			Logger.log(">>> 1. clean folder");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"rd /s /q "+projectName,
					"del /f /s /q "+projectName+".zip"
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
			//2.fis 打包
			Logger.log(">>> 2. fis relase");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"fis3 release build -d ./"+projectName+"/build"
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
			//3.拷贝配置文件
			Logger.log(">>> 3. copy config files");
			String[] resourceFileList = new File(configDir).list();
			for(String rf:resourceFileList){
				FileUtil.copy(configDir+"\\"+rf, configDir+"\\..");
				Logger.log("copy:"+configDir+"\\"+rf);
			}
			
			//4.压缩打包
			Logger.log(">>> 4. compress files to zip");
			ZipUtil.compressDir(new File(projectDir+"\\"+projectName), projectDir+"\\"+projectName+".zip");

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
		//xjp-ws
		Ajida.javaSdkInstall("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-ws");
		
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
		
		
		Ajida.htmlProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_test",
				"/usr/nginx/html", 
				sshConfig);
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
		//xjp-ws
		Ajida.javaSdkInstall("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-ws");
		
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
		
		//xjp-user
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\pro",
				"/usr/local/apache-tomcat-9.0.13",
				sshConfig);
		
		Ajida.startTomcat("/usr/local/apache-tomcat-9.0.13",sshConfig);
		
		Ajida.htmlProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_pro",
				"/var/html", 
				sshConfig);
		
	}
	
	public static void xjp_114_hot() throws Exception{
		System.out.println("enter password:");
		Scanner sc = new Scanner(System.in);
		String password = sc.nextLine();
		sc.close();
		
		SSHConfig sshConfig = new SSHConfig("114.218.158.239", "root", password);
		
		//#定义tomcat节点
		String[] points = {"_1","_2"};
		//#查看现在是tomcat服务哪个节点启动着
		String targetPoint = "";//目标更新节点
		String runningPoint = "";//先在正在运行的节点
		for(String flag:points){
			String pid = SSHUtil.exec(new String[]{
					"ps -ef | grep /usr/local/tomcat"+flag+" | grep java | grep -v grep | awk '{print $2}'"
			}, 60, sshConfig);
			pid = pid !=null?pid.trim():"";
			
			if(StringUtil.isEmpty(pid)){
				//那就是停着的一台机器
				targetPoint = flag;
			}else{
				//就是启动着的机器
				runningPoint = flag;
			}
		}
		if(StringUtil.isEmpty(targetPoint)){
			throw new Exception("没有空闲的tomcat节点");
		}
		
		//#打包代码，上传到停着的节点
		System.out.println("准备更新包到tomcat"+targetPoint);
		//xjp-sdk 依赖包安装
		Ajida.javaSdkInstall("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-sdk");
		//xjp-ws 依赖包安装
		Ajida.javaSdkInstall("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-ws");
		
		//xjp-admin
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin\\config\\pro",
				"/usr/local/tomcat"+targetPoint,
				sshConfig);

		//xjp-collector
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\config\\pro",
				"/usr/local/tomcat"+targetPoint,
				sshConfig);
		
		//xjp-user
		Ajida.javaProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\pro"+targetPoint,
				"/usr/local/tomcat"+targetPoint,
				sshConfig);
		
		Ajida.htmlProjectUpdate(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_pro",
				"/var/html", 
				sshConfig);
		
		//#启动停着的节点
		System.out.println("尝试启动tomcat"+targetPoint);
		SSHUtil.exec(new String[]{
				"/usr/local/tomcat"+targetPoint+"/bin/startup.sh"
				}, 60, sshConfig);
		//监控tomcat启动结果，这里等待10分钟，如果10分钟都没启动成功，则启动失败，不做后续的nignx切换和tomcat停机
		int waitSec = 10*60;
		boolean tomcatStartSuccess = false;
		String tailResult = "";
		for(int i=0;i<waitSec;i++){
			String execResult = SSHUtil.exec(new String[]{
					"tail -n 1 /usr/local/tomcat"+targetPoint+"/logs/catalina.out"
					}, 60, sshConfig);
			execResult = execResult !=null?execResult.trim():"";
			if(!tailResult.equals(execResult)){
				System.out.println(execResult);
				tailResult = execResult;
			}
			if(tailResult != null && tailResult.contains("Server startup in")){
				tomcatStartSuccess = true;
				break;
			}
			Thread.sleep(1000);
		}
		if(tomcatStartSuccess){
			System.out.println("tomcat"+targetPoint+" 启动成功");
		}else{
			throw new Exception("tomcat"+targetPoint+" 启动失败！");
		}
		
		//#切换nginx配置到新启动的节点
		SSHUtil.exec(new String[]{
				//需要复制节点ngix配置文件
				"cp -f /etc/nginx/loadbalance/nginx"+targetPoint+".conf /etc/nginx/nginx.conf",
				"/usr/sbin/nginx -s reload"
				}, 60, sshConfig);
		System.out.println("切换nginx反向代理端口");
		
		//#停掉老的节点
		if(StringUtil.isNotEmpty(runningPoint)){
			String pid = SSHUtil.exec(new String[]{
					"ps -ef | grep /usr/local/tomcat"+runningPoint+" | grep java | grep -v grep | awk '{print $2}'"
					}, 60, sshConfig);
			pid = pid !=null?pid.trim():"";
			while(StringUtil.isNotEmpty(pid)){
				SSHUtil.exec(new String[]{
					"kill -9 "+pid
				} ,60,sshConfig);
				pid = SSHUtil.exec(new String[]{
						"ps -ef | grep /usr/local/tomcat"+runningPoint+" | grep java | grep -v grep | awk '{print $2}'"
						}, 60, sshConfig);
				pid = pid !=null?pid.trim():"";
			}

			System.out.println("停止tomcat"+runningPoint);
		}
		
	}

}
