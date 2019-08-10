package com.ajida;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.axe.util.FileUtil;

public class ScriptUtil {
	public static void main(String[] args) {
		try {
			htmlProjectUpdate(
					"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin", 
					"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\xjp-admin\\js\\conf_test",
					"/usr/nginx/html", 
					new SSHConfig("192.168.199.45", "root", "ybsl1234"));
//			String[] resourceFileList = new File("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\js\\conf_test").list();
//			for(String rf:resourceFileList){
//				FileUtil.copy("D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\js\\conf_test"+"\\"+rf, 
//							  "D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\js\\conf_test"+"\\..\\1.js");
//				Logger.log("copy:"+"D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-html\\xjp-admin\\js\\conf_test"+"\\"+rf);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public static void mvnInstall(String projectDir) throws Exception{
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
	public static void mvnPackage(String projectDir,String configDir) throws Exception{
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
	public static void htmlProjectUpdate(String projectDir,String configDir,String remoteBaseDir,SSHConfig remoteSSHConfig) throws Exception{
		try {
			String result = "";
			String[] cmds = null;
			String projectName = projectDir.substring(projectDir.lastIndexOf("\\")+1);
			
			//================================本地跟新包准备================================
			//1.git更新
			Logger.log(">>> 1. git update");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"git pull"
			};
			result = CmdUtil.exec(cmds);
			if(!result.contains("Already up")){
				//第二次尝试，第一次可能有东西更新下来，不会有Already...
				result = CmdUtil.exec(cmds);
			}
			if(!result.contains("Already up")){
				throw new Exception("<<< error : git update failed");
			}

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
					"fis3 release build -d ./"+projectName
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

			//================================准备更新上传================================
			//5.上传war包
			Logger.log(">>> 5. upload zip file");
			SSHUtil.uploadFile(projectDir+"\\"+projectName+".zip", remoteBaseDir, remoteSSHConfig);
			
			//6.备份远程文件
			Logger.log(">>> 6. backup remote file");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd#HH_mm_ss");
			String backupFileName = projectName+"_"+sdf.format(new Date());
			try {
				SSHUtil.exec(remoteSSHConfig,"mv "+remoteBaseDir+"/"+projectName+" "+remoteBaseDir+"/"+backupFileName,10);
			} catch (Exception e) {}
			
			
			//7.解压缩，删除zip包
			Logger.log(">>> 7. unzip zip and rm zip");
			SSHUtil.exec(remoteSSHConfig,new String[]{
					"unzip -d "+remoteBaseDir+"/"+projectName+" "+remoteBaseDir+"/"+projectName+".zip",
					"rm -rf "+remoteBaseDir+"/"+projectName+".zip"
					},10);
			
			

			//8.清理
			Logger.log(">>> 8. clean folder again");
			cmds = new String[]{
					"cd "+projectDir,
					projectDir.substring(0,2),
					"rd /s /q "+projectName,
					"del /f /s /q "+projectName+".zip"
			};
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {}
			
		} catch (Exception e) {
			throw e;
		}
	}
	
}
