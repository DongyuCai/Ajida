package com.ajida;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.axe.util.FileUtil;
import org.axe.util.StringUtil;

import ch.ethz.ssh2.Connection;

public class Ajida {
	public static void main(String[] args) {
		
	}
	
	/**
	 * @param even 环境，对应/config下的配置文件所在文件夹，比如test、pro
	 * @param appConfig1 第1套配置
	 * @param appConfig2 第2套配置
	 * @param sdkProjectNameAry 依赖的sdk，需要安装的
	 * @param sshConfig 
	 * @param remoteProjectDir
	 * @throws Exception
	 */
	public static void execute(String even,ApplicationConfig appConfig1,ApplicationConfig appConfig2,String[] sdkProjectNameAry,SSHConfig sshConfig,String remoteProjectDir) throws Exception{
		appConfig1.setIndex(1);//设定好顺序
		appConfig2.setIndex(2);
		
		//获取链接
		Connection sshConnection = SSHUtil.connect(sshConfig);
		if(sshConnection == null){
			throw new Exception("连接失败");
		}
		int timeout = 10;
		try {
			//git更新
			String path = new File("").getAbsolutePath();
			String rootPath = path.substring(0,path.lastIndexOf("\\"));
			String projectName = path.substring(path.lastIndexOf("\\")+1);
			
			Ajida.gitPull(rootPath);
			
			//mvn安装sdk工程
			for(String sdkProjectName:sdkProjectNameAry){
				Ajida.mvnInstallJar(rootPath+"\\"+sdkProjectName);
			}

			//根据服务器当前情况，选择使用appConfig1或者appConfig2
			ApplicationConfig appConfig = appConfig1;//默认使用配置1
			String pid = SSHUtil.getPid(remoteProjectDir+"/"+projectName+"_1 | grep java", timeout, sshConnection);
			if(StringUtil.isNotEmpty(pid)){
				//如果查到了配置1 的启动进程，则使用配置2
				appConfig = appConfig2;
			}
			
			
			//mvn打包工程
			String zipName = projectName+"_"+appConfig.getIndex();
			Ajida.mvnPackageJarApplication(
					rootPath+"\\"+projectName,
					rootPath+"\\"+projectName+"\\config\\"+even,
					appConfig,zipName);
			
			//上传到服务器
			sshFileUpload(sshConnection,rootPath+"\\"+projectName+"\\target\\"+zipName+".zip", remoteProjectDir);
			
			//删除远程文件夹
			try {
				SSHUtil.exec(sshConnection,"rm -rf "+remoteProjectDir+"/"+zipName,timeout,false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//解压新包
			Ajida.unzipRemotFile(sshConnection,timeout,remoteProjectDir+"/"+zipName+".zip", remoteProjectDir+"/"+zipName);
			
			//先拷贝nginx配置文件并检查是否ok，如果nginx配置错误，则不能启动app
			try {
				SSHUtil.exec(sshConnection,"cp "+remoteProjectDir+"/"+zipName+"/nginx/* /etc/nginx/vhost",timeout,false);
				
				String result = SSHUtil.exec(sshConnection,"/usr/sbin/nginx -c /etc/nginx/nginx.conf -t",timeout,true);
				if(!result.toUpperCase().contains("SUCCESSFUL")){
					throw new Exception("nginx 配置文件校验失败:\r\n"+result);
				}
			} catch (Exception e) {
				if(e.getMessage().toUpperCase().contains("NO SUCH FILE OR DIRECTORY")){
					throw e;
				}
				if(e.getMessage().toUpperCase().contains("FAILED")){
					throw e;
				}
			}
			
			//启动app
			try {
				SSHUtil.exec(sshConnection, 
						new String[]{
								"cd "+remoteProjectDir+"/"+zipName,
								"chmod 777 -R *",
								"dos2unix start.sh",
		      					"./start.sh"}, 
						timeout,false);
			} catch (Exception e) {}
			System.out.println("正在启动 "+zipName);
			
			//等待启动成功
			Set<String> tailSet = new HashSet<>();//排除tail到的重复行内容
			while(true){
				Thread.sleep(1000);
				try {
					String cat = SSHUtil.exec(sshConnection, "tail -n10 "+remoteProjectDir+"/"+zipName+"/log.txt", timeout,true);
					String[] splitRows = cat.split("\r\n");
					for(String row:splitRows){
						if(!tailSet.contains(row)){
							System.out.println(row);
						}
					}
					tailSet.clear();
					for(String row:splitRows){
						tailSet.add(row);
					}
					if(cat.contains("Axe started success!")){
						System.out.println(">>> "+zipName+"启动成功");
						break;
					}
				} catch (Exception e) {
					if(e.getMessage().toUpperCase().contains("NO SUCH FILE")){
						System.out.print(".");
					}
				}
			}

			//重新启动nginx
			//先拷贝nginx配置文件并检查是否ok，如果nginx配置错误，则不能启动app
			try {
				SSHUtil.exec(sshConnection,"/usr/sbin/nginx -c /etc/nginx/nginx.conf -s reload",timeout,false);
			} catch (Exception e) {}
			System.out.println(">>> Nginx已重启 ");
			
			//停掉老的app
			ApplicationConfig stopConfig = appConfig1;//要停掉的配置，默认节点1
			if(stopConfig.getIndex() == appConfig.getIndex()){
				//如果要停掉的正好是刚启动的，则停掉另一个
				stopConfig = appConfig2;
			}
			String stopZipName = projectName+"_"+stopConfig.getIndex();
			pid = SSHUtil.getPid(remoteProjectDir+"/"+stopZipName+" | grep java", timeout, sshConnection);
			while(StringUtil.isNotEmpty(pid)){
				SSHUtil.exec(sshConnection,"kill -9 "+pid,timeout,false);
				pid = SSHUtil.getPid(remoteProjectDir+"/"+stopZipName+" | grep java", timeout, sshConnection);
			}
			System.out.println(">>> 已停止 "+stopZipName);
			
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				sshConnection.close();
			} catch (Exception e2) {}
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
	public static void mvnPackageJarApplication(String projectPath,String configPath,ApplicationConfig appConfig,String zipName) throws Exception{
		try {
			String projectName = projectPath.substring(projectPath.lastIndexOf("\\")+1);
			
			//1.maven 编译打包
			Logger.log(">>> maven package");
			String[] cmds = new String[]{
					"cd "+projectPath,
					projectPath.substring(0,2),
					"mvn clean package"
			};
			String result = CmdUtil.exec(cmds);
			if(!result.contains("BUILD SUCCESS")){
				throw new Exception("<<< error : maven package failed");
			}
			
			//2.创建一个临时包目录，拷贝jar包到目录中
			Logger.log(">>> create tmp package fold for project");
			cmds = new String[]{
					"cd "+projectPath+"\\target",
					projectPath.substring(0,2),
					"md "+projectName,
					"cd "+projectName,
					"md lib",
					"cd ..",
					"copy "+projectName+".jar "+projectPath+"\\target\\"+projectName+"\\lib\\"
			};
			CmdUtil.exec(cmds);
			
			//3.导出maven依赖
			Logger.log(">>> export referenced libs");
			cmds = new String[]{
					"cd "+projectPath,
					projectPath.substring(0,2),
					"mvn dependency:copy-dependencies -DoutputDirectory="+projectPath+"\\target\\"+projectName+"\\lib"
			};
			CmdUtil.exec(cmds);
			
			//4.拷贝配置文件
			Logger.log(">>> copy config files");
			cmds = new String[]{
					"cd "+projectPath,
					projectPath.substring(0,2),
					"copy "+configPath+"\\*.*"+" "+projectPath+"\\target\\"+projectName+"\\"
			};
			CmdUtil.exec(cmds);
			
			//5.需要特殊处理下nginx配置文件
			File configDir = new File(configPath+"/nginx");
			for(File nginxConfigFile:configDir.listFiles()){
				BufferedReader reader = null;
				BufferedWriter writer = null;
				try {
					reader = new BufferedReader(new FileReader(nginxConfigFile));
					File copyFileDir = new File(projectPath+"\\target\\"+projectName+"\\nginx");
					if(!copyFileDir.exists()){
						copyFileDir.mkdir();
					}
					writer = new BufferedWriter(new FileWriter(new File(copyFileDir,nginxConfigFile.getName())));
					String line = reader.readLine();
					while(line != null){
						for (String key : appConfig.getNginxParams().keySet()) {
							line = line.replaceAll("\\$\\{ *" + key + " *\\}", appConfig.getNginxParams().get(key));
						}
						writer.write(line);
						writer.newLine();
						
						line = reader.readLine();
					}
				} catch (Exception e) {
					throw e;
				} finally {
					if(reader != null){
						try {
							reader.close();
						} catch (Exception e2) {}
					}
					if(writer != null){
						try {
							writer.close();
						} catch (Exception e2) {}
					}
				}
			}
			
			//6.创建启动文件
			Logger.log(">>> create start bat/sh files");
			cmds = new String[]{
					"cd "+projectPath+"\\target\\"+projectName,
					projectPath.substring(0,2),
					//bat
					"echo @echo off>>start.bat",
					"echo set CLASSPATH=.>>start.bat",
					"echo setlocal enabledelayedexpansion>>start.bat",
					"echo FOR %%i IN (\".\\lib\\*.jar\") DO SET CLASSPATH=!CLASSPATH!;%%i>>start.bat",
					"echo java -classpath %CLASSPATH% "+appConfig.getApplicationMainClassAndStartParams()+">>start.bat",
					"echo pause>>start.bat",
					//sh
					"echo SHELL_FOLDER=$(cd \"$(dirname \"$0\")\";pwd)>>start.sh",
					"echo for i in $SHELL_FOLDER/lib/*.jar;>>start.sh",
					"echo do CLASSPATH=$i:\"$CLASSPATH\";>>start.sh",
					"echo done>>start.sh",
					"echo CLASSPATH=:$CLASSPATH>>start.sh",
					"echo java -classpath .:${CLASSPATH} "+appConfig.getApplicationMainClassAndStartParams()+" ^&>>start.sh"
			};
			CmdUtil.exec(cmds);
			
			
			//6.压缩打包
			Logger.log(">>> compress project to zip");
			ZipUtil.compressDir(new File(projectPath+"\\target\\"+projectName), projectPath+"\\target\\"+zipName+".zip");

		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void sshFileBackup(Connection conn,int timeout,String remoteFileDir,String backupFileDir) throws Exception{
		//5.备份远程文件
		Logger.log(">>> ssh backup remote file");
		SSHUtil.exec(conn,"cp -n "+remoteFileDir+" "+backupFileDir,10,false);
	}

	public static void sshFileUpload(Connection conn,String localFileDir,String remoteFileDir) throws Exception{
		//6.上传war包
		Logger.log(">>> upload war file");
		SSHUtil.uploadFile(localFileDir, remoteFileDir, conn);
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
	
	public static void unzipRemotFile(Connection conn,int timeout,String remoteFileDir,String targetDir) throws Exception{
		Logger.log(">>> unzip zip");
		SSHUtil.exec(conn,new String[]{
				"unzip -d "+targetDir+" "+remoteFileDir
				},timeout,false);
	}
	
}
