package com.ajida.util;

import ch.ethz.ssh2.Connection;

public class SSHUtil {
	public static void main(String[] args) {
		try {
			/*SSHUtil.exec(new String[]{
					"cp -n /usr/local/apache-tomcat-9.0.12/webapps/xjp-user.war /usr/local/apache-tomcat-9.0.12/webapps_backup/xjp-user.war_789456"
			}, 60, new SSHConfig("192.168.199.45", "root", "ybsl1234"));*/
			SSHUtil.exec(new String[]{
					"tail -f /usr/local/apache-tomcat-9.0.12/logs/catalina.out"
			}, 60, new SSHConfig("192.168.199.45", "root", "ybsl1234"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 远程执行命令
	 */
	public static String exec(String[] commands,int timeout,SSHConfig conf)throws Exception {
		Connection conn = SSHClient.connect(conf.getIp(),conf.getPort(), conf.getName(), conf.getPassword());
		if(conn == null){
			throw new Exception("连接失败");
		}
		StringBuilder buf = new StringBuilder();
		try {
			for(String cmd:commands){
				String line = SSHClient.exec(conn, cmd ,timeout);
	        	Logger.log(line);
				buf.append(line).append("\r\n");;
			}
		} catch (Exception e) {
			throw e;
		}finally{
			try {
				conn.close();
			} catch (Exception e2) {}
		}
		
		return buf.toString();
	}
	
	/**
	 * 本地文件远程上传
	 */
	public static void uploadFile(String localFilePath, String remotePath,SSHConfig conf)throws Exception{
		Connection conn = SSHClient.connect(conf.getIp(),conf.getPort(), conf.getName(), conf.getPassword());
		if(conn == null){
			throw new Exception("连接失败");
		}
		try {
			SSHClient.upload(conn, localFilePath, remotePath);
		} catch (Exception e) {
			throw e;
		}finally{
			try {
				conn.close();
			} catch (Exception e2) {}
		}
	}
}
