package com.ajida;

import java.io.File;

import org.axe.util.FileUtil;
import org.axe.util.StringUtil;

public class Ajida {
	public static void main(String[] args) {
		
	}
	
	public static String packageOnWindows(String projectName,String mainClassAndStartParams,String[] sdkProjectNameAry) throws Exception{
		//git更新
		String rootPath = new File("").getAbsolutePath();
		rootPath = rootPath.substring(0,rootPath.lastIndexOf("\\"));
		
		Ajida.gitPull(rootPath);
		
		//mvn安装sdk工程
		for(String sdkProjectName:sdkProjectNameAry){
			Ajida.mvnInstallJar(rootPath+"\\"+sdkProjectName);
		}

		//########################xjp-admin
		//mvn打包工程
		Ajida.mvnPackageJarApplication(
				rootPath+"\\"+projectName,
				rootPath+"\\"+projectName+"\\config\\test",
				mainClassAndStartParams);
		
		//返回zip包路径
		return rootPath+"\\"+projectName+"\\target\\"+projectName+".zip";
		
	}
	
	public static void uploadToLinux(String localZipPath,SSHConfig sshConfig,String remoteProjectDir,String projectName) throws Exception{
		//停掉app
		String pid = SSHUtil.exec(sshConfig,"ps -ef | grep "+remoteProjectDir+"/"+projectName+" | grep java | grep -v grep | awk '{print $2}'",10);
		pid = pid !=null?pid.trim():"";
		while(StringUtil.isNotEmpty(pid)){
			SSHUtil.exec(sshConfig,"kill -9 "+pid,10);
			pid = SSHUtil.exec(sshConfig,"ps -ef | grep "+remoteProjectDir+"/"+projectName+" | grep java | grep -v grep | awk '{print $2}'",10);
			pid = pid !=null?pid.trim():"";
		}
		System.out.println("停止 "+projectName);
		
		//删除远程文件夹
		try {
			SSHUtil.exec(sshConfig,"rm -rf "+remoteProjectDir+"/"+projectName,10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//上传新的包
		Ajida.sshFileUpload(localZipPath, remoteProjectDir, sshConfig);
		
		//解压新包
		Ajida.unzipRemotFile(remoteProjectDir+"/"+projectName+".zip", remoteProjectDir+"/"+projectName, sshConfig);
		//启动app
		try {
			SSHUtil.exec(sshConfig, 
					new String[]{
							"cd "+remoteProjectDir+"/"+projectName,
							"chmod 777 -R *",
							"dos2unix start.sh",
	      					"./start.sh"}, 
					10);
		} catch (Exception e) {}
		System.out.println("启动 "+projectName);
		
	}
	
	public static void gitPull(String projectDir) throws Exception{
		//1.git更新
		Logger.log(">>> git update");
		String[] cmds = new String[]{
				"cd "+projectDir,
				projectDir.substring(0,2),
				"git pull"
		};
		String result = CmdUtil.exec(cmds);
		if(!result.contains("Already up")){
			//第二次尝试，第一次可能有东西更新下来，不会有Already...
			result = CmdUtil.exec(cmds);
		}
		if(!result.contains("Already up")){
			throw new Exception("<<< error : git update failed");
		}
		
	}
	
	public static void mvnInstallJar(String projectDir) throws Exception{
		//2.maven 打包
		Logger.log(">>> maven install");
		String[] cmds = new String[]{
				"cd "+projectDir,
				projectDir.substring(0,2),
				"mvn clean install"
		};
		String result = CmdUtil.exec(cmds);
		if(!result.contains("BUILD SUCCESS")){
			throw new Exception("<<< error : maven package failed");
		}
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
	
	/**
	 * 配置本地打包工作
	 * @throws Exception 
	 */
	public static void mvnPackageJarApplication(String projectDir,String configDir,String mainClassPathAndStartParams) throws Exception{
		try {
			String projectName = projectDir.substring(projectDir.lastIndexOf("\\")+1);
			
			//1.maven 编译打包
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
			
			//2.删除配置文件，并压缩打包
//			Logger.log(">>> compress classes to jar");
//			cmds = new String[]{
//					"cd "+projectDir+"\\target\\classes",
//					projectDir.substring(0,2),
//					"del /f /s /q *.*"
//			};
//			try {
//				CmdUtil.exec(cmds);
//			} catch (Exception e) {}
//			ZipUtil.compressDir(new File(projectDir+"\\target\\classes"), projectDir+"\\target\\"+projectName+".jar");
//			
			//3.创建一个临时包目录，拷贝jar包到目录中
			Logger.log(">>> create tmp package fold for project");
			cmds = new String[]{
					"cd "+projectDir+"\\target",
					projectDir.substring(0,2),
					"md "+projectName,
					"cd "+projectName,
					"md lib",
					"cd ..",
					"copy "+projectName+".jar "+projectDir+"\\target\\"+projectName+"\\lib\\"
			};
			CmdUtil.exec(cmds);
			
			//4.导出maven依赖
			Logger.log(">>> export referenced libs");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"mvn dependency:copy-dependencies -DoutputDirectory="+projectDir+"\\target\\"+projectName+"\\lib"
			};
			CmdUtil.exec(cmds);
			
			//5.拷贝配置文件
			Logger.log(">>> copy config files");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"copy "+configDir+"\\*.*"+" "+projectDir+"\\target\\"+projectName+"\\"
			};
			CmdUtil.exec(cmds);
			
			//6.创建启动文件
			Logger.log(">>> create start bat/sh files");
			cmds = new String[]{
					"cd "+projectDir+"\\target\\"+projectName,
					projectDir.substring(0,2),
					//bat
					"echo @echo off>>start.bat",
					"echo set CLASSPATH=.>>start.bat",
					"echo setlocal enabledelayedexpansion>>start.bat",
					"echo FOR %%i IN (\".\\lib\\*.jar\") DO SET CLASSPATH=!CLASSPATH!;%%i>>start.bat",
					"echo java -classpath %CLASSPATH% "+mainClassPathAndStartParams+">>start.bat",
					"echo pause>>start.bat",
					//sh
					"echo for i in ./lib/*.jar;>>start.sh",
					"echo do CLASSPATH=$i:\"$CLASSPATH\";>>start.sh",
					"echo done>>start.sh",
					"echo export CLASSPATH=:$CLASSPATH>>start.sh",
					"echo java -classpath .:${CLASSPATH} "+mainClassPathAndStartParams+" ^&>>start.sh"
			};
			CmdUtil.exec(cmds);
			
			
			//6.压缩打包
			Logger.log(">>> compress project to zip");
			ZipUtil.compressDir(new File(projectDir+"\\target\\"+projectName), projectDir+"\\target\\"+projectName+".zip");

		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void sshFileBackup(String remoteFileDir,String backupFileDir, SSHConfig sshConfig) throws Exception{
		//5.备份远程文件
		Logger.log(">>> ssh backup remote file");
		SSHUtil.exec(sshConfig,"cp -n "+remoteFileDir+" "+backupFileDir,10);
	}

	public static void sshFileUpload(String localFileDir,String remoteFileDir, SSHConfig sshConfig) throws Exception{
		//6.上传war包
		Logger.log(">>> upload war file");
		SSHUtil.uploadFile(localFileDir, remoteFileDir, sshConfig);
	}

	/**
	 * 配置本地打包工作
	 * @throws Exception 
	 */
	public static void htmlPackageZip(String projectDir,String configDir) throws Exception{
		try {
			String projectName = projectDir.substring(projectDir.lastIndexOf("\\")+1);
			
			//1.清理
			Logger.log(">>> clean folder");
			String[] cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"rd /s /q "+projectName,
					"del /f /s /q "+projectName+".zip"
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
			//2.fis 打包
			Logger.log(">>> fis relase");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"fis3 release build -d .\\"+projectName
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
			//3.拷贝配置文件
			Logger.log(">>> copy config files");
			String[] resourceFileList = new File(configDir).list();
			for(String rf:resourceFileList){
				FileUtil.copy(configDir+"\\"+rf, configDir+"\\..");
				Logger.log("copy:"+configDir+"\\"+rf);
			}
			
			//4.压缩打包
			Logger.log(">>> compress files to zip");
			ZipUtil.compressDir(new File(projectDir+"\\"+projectName), projectDir+"\\"+projectName+".zip");

		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void unzipRemotFile(String remoteFileDir,String targetDir, SSHConfig sshConfig) throws Exception{
		Logger.log(">>> unzip zip");
		SSHUtil.exec(sshConfig,new String[]{
				"unzip -d "+targetDir+" "+remoteFileDir
				},10);
	}
	
}
