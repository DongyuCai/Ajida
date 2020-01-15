package com.ajida;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.axe.util.FileUtil;
import org.axe.util.StringUtil;

import ch.ethz.ssh2.Connection;

public class ScriptExecutor {
	public static void main(String[] args) {
		try {
//			xjp_45();
			xjp_114_hot();

			//tomcat路径下静态资源要放到线上
			//定时任务
			//数据脚本，activity_prize的prize_type要刷
			//1.组队签到
			//2.配置抽奖
			//3.后台两个订单查询页面
			//4.新的邀请好友宣传页
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*public static void xjp_45() throws Exception{
		System.out.println("enter password:");
		Scanner sc = new Scanner(System.in);
		String password = sc.nextLine();
		sc.close();
		
		SSHConfig sshConfig = new SSHConfig("192.168.199.45", "root", password);
		
		//停掉tomcat
		String pid = SSHUtil.exec(sshConfig,"ps -ef | grep /usr/local/apache-tomcat-9.0.12 | grep java | grep -v grep | awk '{print $2}'",10);
		pid = pid !=null?pid.trim():"";
		while(StringUtil.isNotEmpty(pid)){
			SSHUtil.exec(sshConfig,"kill -9 "+pid,10);
			pid = SSHUtil.exec(sshConfig,"ps -ef | grep /usr/local/apache-tomcat-9.0.12 | grep java | grep -v grep | awk '{print $2}'",10);
			pid = pid !=null?pid.trim():"";
		}
		System.out.println("停止tomcat");
		
		//git更新
		Ajida.gitPull("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java");
		
		//mvn安装sdk工程
		Ajida.mvnInstallJar("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-sdk");
		Ajida.mvnInstallJar("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-ws");
		

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd#HH_mm_ss");
		//########################xjp-admin
		//mvn打包工程
		Ajida.mvnPackageWarApplication(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin\\config\\test");
		//备份远程文件
		Ajida.sshFileBackup("/usr/local/apache-tomcat-9.0.12/webapps/xjp-admin.war ", "/usr/local/apache-tomcat-9.0.12/webapps_backup/xjp-admin.war_"+sdf.format(new Date()), sshConfig);
		//上传新的包
		Ajida.sshFileUpload("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin\\target\\xjp-admin.war", "/usr/local/apache-tomcat-9.0.12/webapps", sshConfig);

		//########################xjp-collector
		//mvn打包工程
		Ajida.mvnPackageWarApplication(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\config\\test");
		//备份远程文件
		Ajida.sshFileBackup("/usr/local/apache-tomcat-9.0.12/webapps/xjp-collector.war ", "/usr/local/apache-tomcat-9.0.12/webapps_backup/xjp-collector.war_"+sdf.format(new Date()), sshConfig);
		//上传新的包
		Ajida.sshFileUpload("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\target\\xjp-collector.war", "/usr/local/apache-tomcat-9.0.12/webapps", sshConfig);

		//########################xjp-user
		//mvn打包工程
		Ajida.mvnPackageWarApplication(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\test");
		//备份远程文件
		Ajida.sshFileBackup("/usr/local/apache-tomcat-9.0.12/webapps/xjp-user.war ", "/usr/local/apache-tomcat-9.0.12/webapps_backup/xjp-user.war_"+sdf.format(new Date()), sshConfig);
		//上传新的包
		Ajida.sshFileUpload("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\target\\xjp-user.war", "/usr/local/apache-tomcat-9.0.12/webapps", sshConfig);

		//启动tomcat
		System.out.println("尝试启动tomcat");
		SSHUtil.exec(sshConfig,"/usr/local/apache-tomcat-9.0.12/bin/startup.sh",10);
		//监控tomcat启动结果，这里等待10分钟，如果10分钟都没启动成功，则启动失败
		int waitSec = 10*60;
		boolean tomcatStartSuccess = false;
		String tailResult = "";
		for(int i=0;i<waitSec;i++){
			String execResult = SSHUtil.exec(sshConfig,"tail -n 1 /usr/local/apache-tomcat-9.0.12/logs/catalina.out",10);
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
			System.out.println("tomcat 启动成功");
		}else{
			throw new Exception("tomcat 启动失败！");
		}
		
		//更新前端工程
		//git更新
		Ajida.gitPull("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html");
		
		Ajida.htmlPackageZip(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_test");
		//上传新的包
		Ajida.sshFileUpload("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin.zip", "/usr/nginx/html", sshConfig);
		//备份远程文件
		SSHUtil.exec(sshConfig,"mv "+"/usr/nginx/html/xjp-admin /usr/nginx/html/xjp-admin_"+sdf.format(new Date()),10);
		//解压缩远程压缩包
		Ajida.unzipRemotFile("/usr/nginx/html/xjp-admin.zip", "/usr/nginx/html/xjp-admin", sshConfig);
		//清理
		Logger.log(">>> clean folder again");
		String[] cmds = new String[]{
				"cd D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin",
				"D:",
				"rd /s /q xjp-admin",
				"del /f /s /q xjp-admin.zip"
		};
		CmdUtil.exec(cmds);
		
	}*/
	
	public static void xjp_114_hot() throws Exception{
		System.out.println("enter password:");
		Scanner sc = new Scanner(System.in);
		String password = sc.nextLine();
		sc.close();
		
		SSHConfig sshConfig = new SSHConfig("114.218.158.239", "root", password);
		
		Connection connect = SSHUtil.connect(sshConfig);
		
		//#定义tomcat节点
		String[] points = {"_1","_2"};
		//#查看现在是tomcat服务哪个节点启动着
		String targetPoint = "";//目标更新节点
		String runningPoint = "";//先在正在运行的节点
		for(String flag:points){
			String pid = SSHUtil.exec(connect,"ps -ef | grep /usr/local/tomcat"+flag+" | grep java | grep -v grep | awk '{print $2}'",10,true);
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
		

		//git更新
		Ajida.gitPull("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java");
		
		//mvn依赖包安装
		Ajida.mvnInstallJar("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-sdk");
		Ajida.mvnInstallJar("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-ws");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd#HH_mm_ss");
		//########################xjp-admin
		//mvn打包工程
		mvnPackageWarApplication(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin\\config\\pro");
		//备份远程文件
		Ajida.sshFileBackup(connect,10,"/usr/local/tomcat"+targetPoint+"/webapps/xjp-admin.war ", "/usr/local/tomcat"+targetPoint+"/webapps_backup/xjp-admin.war_"+sdf.format(new Date()));
		//上传新的包
		Ajida.sshFileUpload(connect,"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-admin\\target\\xjp-admin.war", "/usr/local/tomcat"+targetPoint+"/webapps/");

		//########################xjp-admin
		//mvn打包工程
		mvnPackageWarApplication(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector",
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\config\\pro");
		//备份远程文件
		Ajida.sshFileBackup(connect,10,"/usr/local/tomcat"+targetPoint+"/webapps/xjp-collector.war ", "/usr/local/tomcat"+targetPoint+"/webapps_backup/xjp-collector.war_"+sdf.format(new Date()));
		//上传新的包
		Ajida.sshFileUpload(connect,"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-collector\\target\\xjp-collector.war", "/usr/local/tomcat"+targetPoint+"/webapps/");

		//########################xjp-admin
		//mvn打包工程
		mvnPackageWarApplication(
			"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
			"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\config\\pro"+targetPoint);
		//备份远程文件
		Ajida.sshFileBackup(connect,10,"/usr/local/tomcat"+targetPoint+"/webapps/xjp-user.war ", "/usr/local/tomcat"+targetPoint+"/webapps_backup/xjp-user.war_"+sdf.format(new Date()));
		//上传新的包
		Ajida.sshFileUpload(connect,"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user\\target\\xjp-user.war", "/usr/local/tomcat"+targetPoint+"/webapps/");

		//#启动停着的节点
		System.out.println("尝试启动tomcat"+targetPoint);
		SSHUtil.exec(connect,"/usr/local/tomcat"+targetPoint+"/bin/startup.sh",10,false);
		//监控tomcat启动结果，这里等待10分钟，如果10分钟都没启动成功，则启动失败，不做后续的nignx切换和tomcat停机
		int waitSec = 10*60;
		boolean tomcatStartSuccess = false;
		String tailResult = "";
		for(int i=0;i<waitSec;i++){
			String execResult = SSHUtil.exec(connect,"tail -n 1 /usr/local/tomcat"+targetPoint+"/logs/catalina.out",10,true);
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
		SSHUtil.exec(connect,new String[]{
				//需要复制节点ngix配置文件
				"cp -f /etc/nginx/loadbalance/nginx"+targetPoint+".conf /etc/nginx/nginx.conf",
				"/usr/sbin/nginx -s reload"
				},10,false);
		System.out.println("切换nginx反向代理端口");
		
		//#停掉老的节点
		if(StringUtil.isNotEmpty(runningPoint)){
			String pid = SSHUtil.exec(connect,"ps -ef | grep /usr/local/tomcat"+runningPoint+" | grep java | grep -v grep | awk '{print $2}'",10,true);
			pid = pid !=null?pid.trim():"";
			while(StringUtil.isNotEmpty(pid)){
				SSHUtil.exec(connect,"kill -9 "+pid,10,false);
				pid = SSHUtil.exec(connect,"ps -ef | grep /usr/local/tomcat"+runningPoint+" | grep java | grep -v grep | awk '{print $2}'",10,true);
				pid = pid !=null?pid.trim():"";
			}

			System.out.println("停止tomcat"+runningPoint);
		}
		/*
		//更新前端工程
		//git更新
		Ajida.gitPull("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html");
		
		Ajida.htmlPackageZip(
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
				"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_pro");
		//上传新的包
		Ajida.sshFileUpload(connect,"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin.zip", "/var/html/xjp-admin.zip");
		//备份远程文件
		SSHUtil.exec(connect,"mv "+"/var/html/xjp-admin /var/html/xjp-admin_"+sdf.format(new Date()),10,false);
		//解压缩远程压缩包
		Ajida.unzipRemotFile(connect,10,"/var/html/xjp-admin.zip", "/var/html/xjp-admin");
		//清理
		Logger.log(">>> clean folder again");
		String[] cmds = new String[]{
				"cd D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin",
				"D:",
				"rd /s /q xjp-admin",
				"del /f /s /q xjp-admin.zip"
		};
		CmdUtil.exec(cmds);
		*/
	}

	
	/**
	 * 配置本地打包工作
	 * @throws Exception 
	 */
	public static void mvnPackageWarApplication(String projectDir,String configDir) throws Exception{
		try {
			String projectName = projectDir.substring(projectDir.lastIndexOf("\\")+1);
			
			//2.maven 打包
			Logger.log(">>> maven package");
			String[] cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"mvn clean package"
			};
			String result = CmdUtil.exec(cmds);
			if(!result.contains("BUILD SUCCESS")){
				throw new Exception("<<< error : maven package failed");
			}
			
			//3.拷贝配置文件
			Logger.log(">>> copy config files");
			String[] resourceFileList = new File(configDir).list();
			for(String rf:resourceFileList){
				FileUtil.copy(configDir+"\\"+rf, projectDir+"\\target\\"+projectName+"\\WEB-INF\\classes");
				Logger.log("copy:"+configDir+"\\"+rf);
			}
			
			//4.压缩打包
			Logger.log(">>> compress files to war");
			ZipUtil.compressDir(new File(projectDir+"\\target\\"+projectName), projectDir+"\\target\\"+projectName+".war");

		} catch (Exception e) {
			throw e;
		}
	}
}
