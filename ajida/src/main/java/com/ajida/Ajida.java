package com.ajida;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.axe.util.FileUtil;
import org.axe.util.StringUtil;

import com.ajida.util.CmdUtil;
import com.ajida.util.Logger;
import com.ajida.util.SSHConfig;
import com.ajida.util.SSHUtil;
import com.ajida.util.ZipUtil;

public class Ajida {
	
	public static void stopTomcat(SSHConfig sshConfig)throws Exception{
		//停止服务器
		Logger.log(">>> stop tomcat");
		String result = SSHUtil.exec(new String[]{
				"ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'"
				}, 60, sshConfig);
		result = result !=null?result.trim():"";
		while(StringUtil.isNotEmpty(result)){
			SSHUtil.exec(new String[]{
				"kill -9 "+result
			} ,60,sshConfig);
			result = SSHUtil.exec(new String[]{
					"ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'"
					}, 60, sshConfig);
			result = result !=null?result.trim():"";
		}
	}
	
	public static void startTomcat(SSHConfig sshConfig)throws Exception{
		//启动tomcat
		Logger.log(">>> startup tomcat");
		String result=SSHUtil.exec(new String[]{
				"/usr/local/apache-tomcat-9.0.12/bin/startup.sh",
				"tail -f /usr/local/apache-tomcat-9.0.12/logs/catalina.out"
				}, 60, sshConfig);// 两分钟没启动完，认为失败
		if(!result.contains("Server startup in")){
			throw new Exception("<<< error : tomcat start failed");
		}
	}
	
	/**
	 * 配置本地打包工作
	 */
	public static void codeUpdate(String projectDir,String configDir,String remoteTomcatDir,SSHConfig remoteSSHConfig){
		try {
			String result = "";
			String[] cmds = null;
			
			//================================本地跟新包准备================================
			//1.git更新
			Logger.log(">>> 1. git update");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"git pull"
			};
			result = CmdUtil.exec(cmds);
			if(!result.contains("Already up-to-date")){
				//第二次尝试，第一次可能有东西更新下来，不会有Already...
				result = CmdUtil.exec(cmds);
			}
			if(!result.contains("Already up-to-date")){
				throw new Exception("<<< error : git update failed");
			}
			
			//2.maven 打包
			Logger.log(">>> 2. maven package");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"mvn clean package"
			};
			result = CmdUtil.exec(cmds);
			if(!result.contains("BUILD SUCCESS")){
				throw new Exception("<<< error : maven package failed");
			}
			
			//3.拷贝配置文件
			Logger.log(">>> 3. copy config files");
			String[] resourceFileList = new File(configDir).list();
			while(projectDir.endsWith("\\") && StringUtil.isNotEmpty(projectDir)){
				projectDir = projectDir.substring(0,projectDir.length()-1);
			}
			String projectName = projectDir.substring(projectDir.lastIndexOf("\\")+1);
			for(String rf:resourceFileList){
				FileUtil.copy(configDir+"\\"+rf, projectDir+"\\target\\"+projectName+"\\WEB-INF\\classes");
				Logger.log("copy:"+configDir+"\\"+rf);
			}
			
			//4.压缩打包
			Logger.log(">>> 4. compress files to war");
			ZipUtil.compressDir(new File(projectDir+"\\target\\"+projectName), projectDir+"\\target\\"+projectName+".war");

			//================================准备更新上传================================
			//5.备份远程文件
			Logger.log(">>> 5. backup remote file");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd#HH_mm_ss");
			String backupFileName = projectName+".war_"+sdf.format(new Date());
			SSHUtil.exec(new String[]{
					"cp -n "+remoteTomcatDir+"/webapps/"+projectName+".war "+remoteTomcatDir+"/webapps_backup/"+backupFileName
					}, 60, remoteSSHConfig);
			
			//6.停止服务器
			/*Logger.log(">>> 6. stop tomcat");
			result = SSHUtil.exec(new String[]{
					"ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'"
					}, 60, remoteSSHConfig);
			result = result !=null?result.trim():"";
			while(StringUtil.isNotEmpty(result)){
				Logger.log("stop:"+result);
				SSHUtil.exec(new String[]{
					"kill -9 "+result
				} ,60,remoteSSHConfig);
				result = SSHUtil.exec(new String[]{
						"ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'"
						}, 60, remoteSSHConfig);
				result = result !=null?result.trim():"";
			}*/

			//6.上传war包
			Logger.log(">>> 6. upload war file");
			SSHUtil.uploadFile(projectDir+"\\target\\"+projectName+".war", remoteTomcatDir+"/webapps", remoteSSHConfig);
			

			//8.启动tomcat 两分钟没启动完，认为失败
			/*Logger.log(">>> 8. startup tomcat");
			SSHUtil.exec(new String[]{
					remoteTomcatDir+"/bin/startup.sh"
					}, 60, remoteSSHConfig);*/
			/*if(!result.contains("Server startup in")){
				throw new Exception("<<< error : tomcat start failed");
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*public static void update(String resourceDir, String targetDir, String project, String linuxIp, String linuxUsername, String linuxPassword, String tomcatDir, boolean restartTomcat) throws Exception{
		//拷贝配置文件
		String[] resourceFileList = new File(resourceDir).list();
		for(String rf:resourceFileList){
			FileUtil.copy(resourceDir+"\\"+rf, targetDir+project+"\\WEB-INF\\classes");
			System.out.println("成功拷贝"+rf);
		}
		
		ZipUtil.compressDir(new File(targetDir+project), new File(targetDir+project+".war"));
		//建立远程连接
		Connection conn = SSHClient.connect(linuxIp,22, linuxUsername, linuxPassword);
		//备份远程文件
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd#HH_mm_ss");
		String backupFileName = project.substring(1)+".war_"+sdf.format(new Date());
		SSHClient.exec(conn, "cp -n "+tomcatDir+"/webapps/"+project.substring(1)+".war "+tomcatDir+"/webapps_backup/"+backupFileName+".war ", 10);
		System.out.println("备份"+tomcatDir+"/webapps/"+project.substring(1)+".war "+tomcatDir+"/webapps_backup/"+backupFileName+".war ");
		//获取tomcat pid并杀掉
		String pid = SSHClient.exec(conn, "ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'" ,20);
		pid = pid !=null?pid.trim():"";
		while(StringUtil.isNotEmpty(pid)){
			System.out.println("停止tomcat进程"+pid);
			SSHClient.exec(conn, "kill -9 "+pid ,10);
			pid = SSHClient.exec(conn, "ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'" ,20);
			pid = pid !=null?pid.trim():"";
		}
		//上传war包
		SSHClient.upload(conn, targetDir+project+".war", tomcatDir+"/webapps");
		if(restartTomcat){
			//启动tomcat
			System.out.println("重启tomcat");
			SSHClient.exec(conn, tomcatDir+"/bin/startup.sh" ,20);
		}
		conn.close();
	}*/
	
}
