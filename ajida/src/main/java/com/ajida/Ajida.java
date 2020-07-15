package com.ajida;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.axe.util.LogUtil;
import org.axe.util.StringUtil;

import ch.ethz.ssh2.Connection;

public class Ajida {

	/**
	 * 交换启动节点
	 */
	public static void exchangeServerPoint(SSHConfig sshConfig, String distDir,String projectName){
		Connection sshConnection = null;
		try {
			// 获取链接
			sshConnection = SSHUtil.connect(sshConfig);
			if (sshConnection == null) {
				throw new Exception("连接失败");
			}
			int timeout = 10;
			//1.查看目前启动的是哪个节点
			int pointIndexNow = 1;
			String pid = SSHUtil.getPid(distDir + "/" + projectName + "_"+pointIndexNow+" | grep java", timeout,
					sshConnection);
			if (StringUtil.isEmpty(pid)) {
				// 如果不是节点1正启动着，就认为是节点2在运行，不管节点2是否真的在运行
				pointIndexNow = 2;
			}

			//要启动的节点
			int pointIndexStart = pointIndexNow==1?2:1;
//			int pointIndexStop = pointIndexNow==1?1:2;
			
			// 启动app
			try {
				SSHUtil.exec(sshConnection, new String[] { "cd " + distDir + "/" + projectName + "_"+ pointIndexStart, "chmod 777 -R *",
						"dos2unix start.sh", "./start.sh" }, timeout, false);
			} catch (Exception e) {
			}
			LogUtil.log("正在启动 " + projectName + "_"+ pointIndexStart);

			// 等待启动成功
			Set<String> tailSet = new HashSet<>();// 排除tail到的重复行内容
			while (true) {
				Thread.sleep(1000);
				try {
					String cat = SSHUtil.exec(sshConnection,
							"tail -n10 " + distDir + "/" + projectName + "_"+ pointIndexStart + "/log.txt", timeout, true);
					String[] splitRows = cat.split("\r\n");
					for (String row : splitRows) {
						if (!tailSet.contains(row)) {
							LogUtil.log(row);
						}
					}
					tailSet.clear();
					for (String row : splitRows) {
						tailSet.add(row);
					}
					if (cat.contains("Axe started success!")) {
						LogUtil.log(">>> " + projectName + "_"+ pointIndexStart + "启动成功");
						break;
					}
				} catch (Exception e) {
					if (e.getMessage().toUpperCase().contains("NO SUCH FILE")) {
						System.out.print(".");
					}
				}
			}

			// 先拷贝nginx配置文件并检查是否ok，如果nginx配置错误，则不能启动app
			try {
				SSHUtil.exec(sshConnection, "cp " + distDir + "/" + projectName + "_"+ pointIndexStart + "/nginx/* /etc/nginx/vhost",
						timeout, false);
				
				String result = SSHUtil.exec(sshConnection, "/usr/sbin/nginx -c /etc/nginx/nginx.conf -t", timeout,
						true);
				if (!result.toUpperCase().contains("SUCCESSFUL")) {
					throw new Exception("nginx 配置文件校验失败:\r\n" + result);
				}
			} catch (Exception e) {
				if (e.getMessage().toUpperCase().contains("NO SUCH FILE OR DIRECTORY")) {
					throw e;
				}
				if (e.getMessage().toUpperCase().contains("FAILED")) {
					throw e;
				}
			}
			
			// 重新启动nginx
			try {
				SSHUtil.exec(sshConnection, "/usr/sbin/nginx -c /etc/nginx/nginx.conf -s reload", timeout, false);
			} catch (Exception e) {
			}
			LogUtil.log(">>> Nginx已重启 ");

			// 停掉老的app
			/*pid = SSHUtil.getPid(distDir + "/" + projectName + "_"+ pointIndexStop + " | grep java", timeout, sshConnection);
			while (StringUtil.isNotEmpty(pid)) {
				SSHUtil.exec(sshConnection, "kill -9 " + pid, timeout, false);
				pid = SSHUtil.getPid(distDir + "/" + projectName + "_"+ pointIndexStop + " | grep java", timeout, sshConnection);
			}
			LogUtil.log(">>> 已停止 " + projectName + "_"+ pointIndexStop);
			LogUtil.log(">>> 切换完成 <<<");*/
		} catch (Exception e) {
			LogUtil.error(e);
		} finally {
			try {
				if(sshConnection != null){
					sshConnection.close();
				}
			} catch (Exception e2) {
			}
		}
		
	}
	
	/**
	 * @param even
	 *            环境，对应/config下的配置文件所在文件夹，比如test、pro
	 * @param appConfig1
	 *            第1套配置
	 * @param appConfig2
	 *            第2套配置
	 * @param sdkProjectNameAry
	 *            依赖的sdk，需要安装的
	 * @param sshConfig
	 * @param distDir 发布目录
	 * @throws Exception
	 */
	public static void axeProjectUpdate(String even, AxeAppConfig appConfig1, AxeAppConfig appConfig2,
			String[] sdkProjectNameAry, SSHConfig sshConfig, String distDir) throws Exception {
		axeProjectUpdate(true, true, even, appConfig1, appConfig2, sdkProjectNameAry, sshConfig, distDir);
	}

	public static void axeProjectUpdate(boolean needGitPull, boolean needStopAnotherPoint, String even, AxeAppConfig appConfig1,
			AxeAppConfig appConfig2, String[] sdkProjectNameAry, SSHConfig sshConfig, String distDir)
			throws Exception {
		appConfig1.setIndex(1);// 设定好顺序
		appConfig2.setIndex(2);

		// 获取链接
		Connection sshConnection = SSHUtil.connect(sshConfig);
		if (sshConnection == null) {
			throw new Exception("连接失败");
		}
		int timeout = 10;
		try {
//			SSHUtil.exec(sshConnection, "/", timeout, true);
			
			String path = new File("").getAbsolutePath();
			String rootPath = path.substring(0, path.lastIndexOf("\\"));
			String projectName = path.substring(path.lastIndexOf("\\") + 1);

			// 根据服务器当前情况，选择使用appConfig1或者appConfig2
			AxeAppConfig appConfig = appConfig1;// 默认使用配置1
			String pid = SSHUtil.getPid(distDir + "/" + projectName + "_1 | grep java", timeout,
					sshConnection);
			if (StringUtil.isNotEmpty(pid)) {
				// 如果查到了配置1 的启动进程，则使用配置2
				appConfig = appConfig2;
			}

			// git更新
			if (needGitPull) {
				gitPull(rootPath);
			}

			// mvn安装sdk工程
			for (String sdkProjectName : sdkProjectNameAry) {
				mvnInstallJar(rootPath + "\\" + sdkProjectName);
			}

			// mvn打包工程
			String zipName = projectName + "_" + appConfig.getIndex();
			boolean needConfigNginx = mvnPackageJarApplication(rootPath + "\\" + projectName, rootPath + "\\" + projectName + "\\config\\" + even,
					appConfig, zipName);

			// 上传到服务器
			sshFileUpload(sshConnection, rootPath + "\\" + projectName + "\\target\\" + zipName + ".zip",
					distDir);

			// 删除远程文件夹
			try {
				SSHUtil.exec(sshConnection, "rm -rf " + distDir + "/" + zipName, timeout, false);
			} catch (Exception e) {
				LogUtil.error(e);
			}

			// 解压新包
			unzipRemotFile(sshConnection, timeout, distDir + "/" + zipName + ".zip",
					distDir + "/" + zipName);

			// 先拷贝nginx配置文件并检查是否ok，如果nginx配置错误，则不能启动app
			if(needConfigNginx){
				try {
					SSHUtil.exec(sshConnection, "cp " + distDir + "/" + zipName + "/nginx/* /etc/nginx/vhost",
							timeout, false);
					
					String result = SSHUtil.exec(sshConnection, "/usr/sbin/nginx -c /etc/nginx/nginx.conf -t", timeout,
							true);
					if (!result.toUpperCase().contains("SUCCESSFUL")) {
						throw new Exception("nginx 配置文件校验失败:\r\n" + result);
					}
				} catch (Exception e) {
					if (e.getMessage().toUpperCase().contains("NO SUCH FILE OR DIRECTORY")) {
						throw e;
					}
					if (e.getMessage().toUpperCase().contains("FAILED")) {
						throw e;
					}
				}
			}

			// 启动app
			try {
				SSHUtil.exec(sshConnection, new String[] { "cd " + distDir + "/" + zipName, "chmod 777 -R *",
						"dos2unix start.sh", "./start.sh" }, timeout, false);
			} catch (Exception e) {
			}
			LogUtil.log("正在启动 " + zipName);

			// 等待启动成功
			Set<String> tailSet = new HashSet<>();// 排除tail到的重复行内容
			while (true) {
				Thread.sleep(1000);
				try {
					String cat = SSHUtil.exec(sshConnection,
							"tail -n10 " + distDir + "/" + zipName + "/log.txt", timeout, true);
					String[] splitRows = cat.split("\r\n");
					for (String row : splitRows) {
						if (!tailSet.contains(row)) {
							LogUtil.log(row);
						}
					}
					tailSet.clear();
					for (String row : splitRows) {
						tailSet.add(row);
					}
					if (cat.contains("Axe started success!")) {
						LogUtil.log(">>> " + zipName + "启动成功");
						break;
					}
				} catch (Exception e) {
					if (e.getMessage().toUpperCase().contains("NO SUCH FILE")) {
						System.out.print(".");
					}
				}
			}

			if(needConfigNginx){
				// 重新启动nginx
				// 先拷贝nginx配置文件并检查是否ok，如果nginx配置错误，则不能启动app
				try {
					SSHUtil.exec(sshConnection, "/usr/sbin/nginx -c /etc/nginx/nginx.conf -s reload", timeout, false);
				} catch (Exception e) {
				}
				LogUtil.log(">>> Nginx已重启 ");
			}

			// 停掉老的app
			//等待10秒钟，等待所有请求已经处理完毕
			if(needStopAnotherPoint){
				Thread.sleep(10000);
				AxeAppConfig stopConfig = appConfig1;// 要停掉的配置，默认节点1
				if (stopConfig.getIndex() == appConfig.getIndex()) {
					// 如果要停掉的正好是刚启动的，则停掉另一个
					stopConfig = appConfig2;
				}
				String stopZipName = projectName + "_" + stopConfig.getIndex();
				pid = SSHUtil.getPid(distDir + "/" + stopZipName + " | grep java", timeout, sshConnection);
				while (StringUtil.isNotEmpty(pid)) {
					SSHUtil.exec(sshConnection, "kill -9 " + pid, timeout, false);
					pid = SSHUtil.getPid(distDir + "/" + stopZipName + " | grep java", timeout, sshConnection);
				}
				LogUtil.log(">>> 已停止 " + stopZipName);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				sshConnection.close();
			} catch (Exception e2) {
			}
		}
	}

	public static void htmlProjectUpdate(String even, SSHConfig sshConfig, String projectLocalDir,
			String remoteProjectDir) throws Exception {
		htmlProjectUpdate(true, even, sshConfig, projectLocalDir, remoteProjectDir,null,null);
	}
	

	public static void htmlProjectUpdate(
			String even, SSHConfig sshConfig, String projectLocalDir,
			String remoteProjectDir,
			String[] stringReplaceFilePathAry,//可以绝对路径，也可以相对路径
			String[] stringReplaceRuleAry//需要版本替换的结尾，不一定只能是后缀，可以带文件名甚至更多一点，甚至可以正则
			) throws Exception {
		htmlProjectUpdate(true, even, sshConfig, projectLocalDir, remoteProjectDir,stringReplaceFilePathAry,stringReplaceRuleAry);
	}

	public static void htmlProjectUpdate(
			boolean needGitPull, 
			String even, 
			SSHConfig sshConfig,
			String projectLocalDir,
			String remoteProjectDir,
			String[] stringReplaceFilePathAry,//可以绝对路径，也可以相对路径，但是相对路径要以/或者\\开头
			String[] stringReplaceRuleAry//字符串替换规则，规则：结果，比如\.js:.js?v=123 表示.js都替换成.js?v=123
			) throws Exception {
		// 获取链接
		Connection sshConnection = SSHUtil.connect(sshConfig);
		if (sshConnection == null) {
			throw new Exception("连接失败");
		}
		int timeout = 10;
		try {

			String projectName = projectLocalDir.substring(projectLocalDir.lastIndexOf("\\") + 1);

			// git更新
			if (needGitPull) {
				gitPull(projectLocalDir);
			}

			// 清理
			LogUtil.log(">>> clean folder");
			String[] cmds = new String[] { "cd " + projectLocalDir, projectLocalDir.substring(0, 2),
					"rd /s /q " + projectName, "del /f /s /q " + projectName + ".zip" };
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {
			}

			// fis 打包
			LogUtil.log(">>> fis relase");
			cmds = new String[] { "cd " + projectLocalDir, projectLocalDir.substring(0, 2),
					"fis3 release build -d .\\" + projectName };
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {
			}

			// 拷贝配置文件
			LogUtil.log(">>> copy config files");
			cmds = new String[] { "cd " + projectLocalDir, projectLocalDir.substring(0, 2), "copy " + projectLocalDir
					+ "\\config\\" + even + "\\*.*" + " " + projectLocalDir + "\\" + projectName + "\\" };
			CmdUtil.exec(cmds);

			// 对文件进行规则内容替换
			if(stringReplaceFilePathAry != null && stringReplaceRuleAry != null){
				for(String filePath:stringReplaceFilePathAry){
					filePath = filePath.replaceAll("/","\\\\");
					if(filePath.startsWith("\\")){
						//绝对路径进行补充
						filePath = projectLocalDir + "\\" + projectName+filePath;
					}
					
					File oldFile = new File(filePath);
					File tmpFile = new File(filePath+".ajida_"+StringUtil.getRandomString(4,"0123456789"));
					oldFile.renameTo(tmpFile);
					//开始复制并且替换版本号
					BufferedReader reader = null;
					BufferedWriter writer = null;
					try {
						reader = new BufferedReader(new FileReader(tmpFile));
						writer = new BufferedWriter(new FileWriter(new File(filePath)));
						String line = reader.readLine();
						while(line != null){
							for(String rule:stringReplaceRuleAry){
								String[] split = rule.split(":");
								line = line.replaceAll(split[0], split[1]);
							}
							writer.write(line);
							writer.newLine();
							line = reader.readLine();
						}
						
						oldFile.delete();//正常复制并替换内容结束，旧文件就删除了
					} catch (Exception e) {
						throw e;
					}finally{
						if(reader != null){
							try {
								reader.close();
							} catch (Exception e2) {
							}
						}
						if(writer != null){
							try {
								writer.close();
							} catch (Exception e2) {
							}
						}
					}
				}
			}

			// 压缩打包
			LogUtil.log(">>> compress files to zip");
			ZipUtil.compressDir(new File(projectLocalDir + "\\" + projectName),
					projectLocalDir + "\\" + projectName + ".zip",new HashSet<String>());

			// 上传新的包
			sshFileUpload(sshConnection, projectLocalDir + "\\" + projectName + ".zip", remoteProjectDir);

			// 删除远程文件夹
			try {
				SSHUtil.exec(sshConnection, "rm -rf " + remoteProjectDir + "/" + projectName, timeout, false);
			} catch (Exception e) {
				LogUtil.error(e);
			}

			// 解压缩远程压缩包
			unzipRemotFile(sshConnection, timeout, remoteProjectDir + "/" + projectName + ".zip",
					remoteProjectDir + "/" + projectName);
			// 清理
			LogUtil.log(">>> clean folder again");
			cmds = new String[] { "cd " + projectLocalDir, projectLocalDir.substring(0, 2), "rd /s /q " + projectName,
					"del /f /s /q " + projectName + ".zip" };
			try {
				CmdUtil.exec(cmds);
			} catch (Exception e) {
			}

		} catch (Exception e) {
			throw e;
		}
	}

	public static void gitPull(String projectDir) throws Exception {
		// 1.git更新
		LogUtil.log(">>> git update");
		String[] cmds = new String[] { "cd " + projectDir, projectDir.substring(0, 2), "git pull" };
		String result = CmdUtil.exec(cmds);
		if (!result.contains("Already up")) {
			// 第二次尝试，第一次可能有东西更新下来，不会有Already...
			result = CmdUtil.exec(cmds);
		}
		if (!result.contains("Already up")) {
			throw new Exception("<<< error : git update failed");
		}

	}

	public static void mvnInstallJar(String projectDir) throws Exception {
		// 2.maven 打包
		LogUtil.log(">>> maven install");
		String[] cmds = new String[] { "cd " + projectDir, projectDir.substring(0, 2), "mvn clean install" };
		String result = CmdUtil.exec(cmds);
		if (!result.contains("BUILD SUCCESS")) {
			throw new Exception("<<< error : maven package failed");
		}
	}

	/**
	 * 配置本地打包工作
	 * 
	 * @throws Exception
	 */
	public static boolean mvnPackageJarApplication(String projectPath, String configPath, AxeAppConfig appConfig,
			String zipName) throws Exception {
		try {
			String projectName = projectPath.substring(projectPath.lastIndexOf("\\") + 1);

			// 1.maven 编译打包
			LogUtil.log(">>> maven package");
			String[] cmds = new String[] { "cd " + projectPath, projectPath.substring(0, 2), "mvn clean package" };
			String result = CmdUtil.exec(cmds);
			if (!result.contains("BUILD SUCCESS")) {
				throw new Exception("<<< error : maven package failed");
			}

			// 2.创建一个临时包目录，拷贝jar包到目录中
			LogUtil.log(">>> create tmp package fold for project");
			cmds = new String[] { "cd " + projectPath + "\\target", projectPath.substring(0, 2), "md " + projectName,
					"cd " + projectName, "md lib", "cd ..",
					"copy " + projectName + ".jar " + projectPath + "\\target\\" + projectName + "\\lib\\" };
			CmdUtil.exec(cmds);

			// 3.导出maven依赖
			LogUtil.log(">>> export referenced libs");
			cmds = new String[] { "cd " + projectPath, projectPath.substring(0, 2),
					"mvn dependency:copy-dependencies -DoutputDirectory=" + projectPath + "\\target\\" + projectName
							+ "\\lib" };
			CmdUtil.exec(cmds);

			// 4.拷贝配置文件
			LogUtil.log(">>> copy config files");
			cmds = new String[] { "cd " + projectPath, projectPath.substring(0, 2),
					"xcopy " + configPath + " " + projectPath + "\\target\\" + projectName + " /e" };
			CmdUtil.exec(cmds);

			// 4.1替换掉配置文件中的变量
			File configDir = new File(configPath + " " + projectPath + "\\target\\" + projectName);
			for (File configFile : configDir.listFiles()) {
				if(configFile.isFile() && configFile.getName().endsWith(".properties")){
					StringBuilder buf = new StringBuilder();
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(configFile));
						String line = reader.readLine();
						while(line != null){
							if(buf.length() > 0){
								buf.append("#LINE#");
							}
							buf.append(line);
							
							line = reader.readLine();
						}
					} catch (Exception e) {
						LogUtil.error(e);
					} finally {
						if(reader != null){
							try {
								reader.close();
							} catch (Exception e2) {}
						}
					}
					
					if(buf.length() > 0){
						BufferedWriter writer = null;
						try{
							writer = new BufferedWriter(new FileWriter(configFile));
							String[] split = buf.toString().split("#LINE#");
							for(String line:split){
								writer.write(line);
								writer.newLine();
							}
						} catch (Exception e) {
							LogUtil.error(e);
						} finally {
							if(writer != null){
								try {
									writer.close();
								} catch (Exception e2) {}
							}
						}
					}
				}
			}

			// 5.需要特殊处理下nginx配置文件
			File nginxConfigDir = new File(configPath + "\\nginx");
			if(nginxConfigDir.exists()){
				for (File nginxConfigFile : nginxConfigDir.listFiles()) {
					BufferedReader reader = null;
					BufferedWriter writer = null;
					try {
						reader = new BufferedReader(new FileReader(nginxConfigFile));
						File copyFileDir = new File(projectPath + "\\target\\" + projectName + "\\nginx");
						if (!copyFileDir.exists()) {
							copyFileDir.mkdir();
						}
						writer = new BufferedWriter(new FileWriter(new File(copyFileDir, nginxConfigFile.getName())));
						String line = reader.readLine();
						while (line != null) {
							for (String key : appConfig.getConfigParams().keySet()) {
								line = line.replaceAll("\\$\\{ *" + key + " *\\}", appConfig.getConfigParams().get(key));
							}
							writer.write(line);
							writer.newLine();

							line = reader.readLine();
						}
					} catch (Exception e) {
						throw e;
					} finally {
						if (reader != null) {
							try {
								reader.close();
							} catch (Exception e2) {
							}
						}
						if (writer != null) {
							try {
								writer.close();
							} catch (Exception e2) {
							}
						}
					}
				}
			}

			// 6.创建启动文件
			LogUtil.log(">>> create start bat/sh files");
			cmds = new String[] { "cd " + projectPath + "\\target\\" + projectName, projectPath.substring(0, 2),
					// bat
					"echo @echo off>>start.bat", "echo set CLASSPATH=.>>start.bat",
					"echo setlocal enabledelayedexpansion>>start.bat",
					"echo FOR %%i IN (\".\\lib\\*.jar\") DO SET CLASSPATH=!CLASSPATH!;%%i>>start.bat",
					"echo java -classpath %CLASSPATH% " + appConfig.getApplicationMainClassAndStartParams()
							+ ">>start.bat",
					"echo pause>>start.bat",
					// sh
					"echo SHELL_FOLDER=$(cd \"$(dirname \"$0\")\";pwd)>>start.sh",
					"echo for i in $SHELL_FOLDER/lib/*.jar;>>start.sh",
					"echo do CLASSPATH=$i:\"$CLASSPATH\";>>start.sh", "echo done>>start.sh",
					"echo CLASSPATH=:$CLASSPATH>>start.sh", "echo java -classpath .:${CLASSPATH} "
							+ appConfig.getApplicationMainClassAndStartParams() + " ^&>>start.sh" };
			CmdUtil.exec(cmds);

			// 6.压缩打包
			LogUtil.log(">>> compress project to zip");
			ZipUtil.compressDir(new File(projectPath + "\\target\\" + projectName),
					projectPath + "\\target\\" + zipName + ".zip",new HashSet<String>());

			return nginxConfigDir.exists();
		} catch (Exception e) {
			throw e;
		}
	}

	public static void sshFileBackup(Connection conn, int timeout, String remoteFileDir, String backupFileDir)
			throws Exception {
		// 5.备份远程文件
		LogUtil.log(">>> ssh backup remote file");
		SSHUtil.exec(conn, "cp -n " + remoteFileDir + " " + backupFileDir, timeout, false);
	}

	public static void sshFileUpload(Connection conn, String localFileDir, String remoteFileDir) throws Exception {
		// 6.上传war包
		LogUtil.log(">>> upload war file");
		SSHUtil.uploadFile(localFileDir, remoteFileDir, conn);
	}

	public static void unzipRemotFile(Connection conn, int timeout, String remoteFileDir, String targetDir)
			throws Exception {
		LogUtil.log(">>> unzip zip");
		SSHUtil.exec(conn, new String[] { "unzip -d " + targetDir + " " + remoteFileDir }, timeout, false);
	}

}
