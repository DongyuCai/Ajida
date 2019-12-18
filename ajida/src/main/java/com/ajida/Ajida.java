package com.ajida;

import java.io.File;

import org.axe.util.FileUtil;

public class Ajida {
	public static void main(String[] args) {
		
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
			String projectName = projectDir.substring(projectDir.lastIndexOf("/")+1);
			
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
				FileUtil.copy(configDir+"/"+rf, projectDir+"/target/"+projectName+"/WEB-INF/classes");
				Logger.log("copy:"+configDir+"/"+rf);
			}
			
			//4.压缩打包
			Logger.log(">>> compress files to war");
			ZipUtil.compressDir(new File(projectDir+"/target/"+projectName), projectDir+"/target/"+projectName+".war");

		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 配置本地打包工作
	 * @throws Exception 
	 */
	public static void mvnPackageJarApplication(String projectDir,String configDir) throws Exception{
		try {
			String projectName = projectDir.substring(projectDir.lastIndexOf("/")+1);
			
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
			
			//3.创建一个临时包目录，拷贝jar包到目录中
			Logger.log(">>> create tmp package fold for project");
			cmds = new String[]{
					"cd "+projectDir+"/target",
					projectDir.substring(0,2),
					"md "+projectDir,
					"cd "+projectDir,
					"md lib",
					"cd ..",
					"copy "+projectName+".jar ./"+projectName+"/lib/"+projectName+".jar"
			};
			
			//4.导出maven依赖
			Logger.log(">>> export referenced libs");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"mvn dependency:copy-dependencies -DoutputDirectory=target/"+projectName+"/lib"
			};
			
			//5.拷贝配置文件
			Logger.log(">>> copy config files");
			String[] resourceFileList = new File(configDir).list();
			for(String rf:resourceFileList){
				FileUtil.copy(configDir+"/"+rf, projectDir+"/target/"+projectName+"/");
				Logger.log("copy:"+configDir+"/"+rf);
			}
			
			//6.压缩打包
			Logger.log(">>> compress files to war");
			ZipUtil.compressDir(new File(projectDir+"/target/"+projectName), projectDir+"/target/"+projectName+".zip");

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
			String projectName = projectDir.substring(projectDir.lastIndexOf("/")+1);
			
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
					"fis3 release build -d ./"+projectName
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
			//3.拷贝配置文件
			Logger.log(">>> copy config files");
			String[] resourceFileList = new File(configDir).list();
			for(String rf:resourceFileList){
				FileUtil.copy(configDir+"/"+rf, configDir+"/..");
				Logger.log("copy:"+configDir+"/"+rf);
			}
			
			//4.压缩打包
			Logger.log(">>> compress files to zip");
			ZipUtil.compressDir(new File(projectDir+"/"+projectName), projectDir+"/"+projectName+".zip");

		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void unzipRemotFile(String remoteFileDir,String targetDir, SSHConfig sshConfig) throws Exception{
		Logger.log(">>> unzip zip and rm zip");
		SSHUtil.exec(sshConfig,new String[]{
				"unzip -d "+targetDir+" "+remoteFileDir,
				"rm -rf "+remoteFileDir+".zip"
				},10);
	}
	
}
