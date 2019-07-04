package com.ajida.util;

import java.util.List;

import ch.ethz.ssh2.Connection;

public class SSHUtil {
	public static void main(String[] args) {
		try {
			/*SSHUtil.exec(new String[]{
					"cp -n /usr/local/apache-tomcat-9.0.12/webapps/xjp-user.war /usr/local/apache-tomcat-9.0.12/webapps_backup/xjp-user.war_789456"
			}, 60, new SSHConfig("192.168.199.45", "root", "ybsl1234"));*/
			/*SSHUtil.exec(new String[]{
					"tail -f /usr/local/apache-tomcat-9.0.12/logs/catalina.out"
			}, 60, new SSHConfig("192.168.199.45", "root", "ybsl1234"));*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取连接
	 */
	public static Connection connect(SSHConfig conf) throws Exception{
		Connection conn = SSHClient.connect(conf.getIp(),conf.getPort(), conf.getName(), conf.getPassword());
		return conn;
	}
	
	/**
	 * 远程执行命令
	 */
	public static String exec(SSHConfig conf,String command,int timeout)throws Exception {
		return exec(conf, command, timeout, true);
	}
	public static String exec(SSHConfig conf,String command,int timeout,boolean needOutput)throws Exception {
		Connection conn = null;
		try {
			conn = SSHClient.connect(conf.getIp(),conf.getPort(), conf.getName(), conf.getPassword());
			return SSHClient.exec(conn, command, timeout,needOutput);
		} catch (Exception e) {
			throw e;
		}finally{
			try {
				if(conn != null){
					conn.close();
				}
			} catch (Exception e2) {}
		}
	}
	
	/**
	 * 远程执行命令
	 */
	public static String exec(SSHConfig conf,String[] commands,int timeout)throws Exception {
		Connection conn = null;
		try {
			conn = SSHClient.connect(conf.getIp(),conf.getPort(), conf.getName(), conf.getPassword());
			return exec(conn, commands, timeout);
		} catch (Exception e) {
			throw e;
		}finally{
			try {
				if(conn != null){
					conn.close();
				}
			} catch (Exception e2) {}
		}
	}
	
	/**
	 * 远程执行命令
	 */
	public static String exec(SSHConfig conf,List<String> commands,int timeout)throws Exception {
		Connection conn = null;
		try {
			conn = SSHClient.connect(conf.getIp(),conf.getPort(), conf.getName(), conf.getPassword());
			return exec(conn, commands, timeout);
		} catch (Exception e) {
			throw e;
		}finally{
			try {
				if(conn != null){
					conn.close();
				}
			} catch (Exception e2) {}
		}
	}
	
	/**
	 * 远程执行命令
	 */
	public static String exec(Connection conn,String command,int timeout)throws Exception {
		return exec(conn, command, timeout, true);
	}
	public static String exec(Connection conn,String command,int timeout,boolean needOutput)throws Exception {
		try {
			return SSHClient.exec(conn, command ,timeout,needOutput);
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * 远程执行命令
	 */
	public static String exec(Connection conn,String[] commands,int timeout)throws Exception {
		return exec(conn, commands, timeout, true);
	}
	public static String exec(Connection conn,String[] commands,int timeout,boolean needOutput)throws Exception {
		try {
			StringBuilder buf = new StringBuilder();
			for(String cmd:commands){
				if(buf.length()>0){
					buf.append(";");
				}
				buf.append(cmd);
			}
			
			return SSHClient.exec(conn, buf.toString() ,timeout,needOutput);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 远程执行命令
	 */
	public static String exec(Connection conn,List<String> commands,int timeout)throws Exception {
		return exec(conn, commands, timeout, true);
	}
	public static String exec(Connection conn,List<String> commands,int timeout,boolean needOutput)throws Exception {
		try {
			StringBuilder buf = new StringBuilder();
			for(String cmd:commands){
				if(buf.length()>0){
					buf.append(";");
				}
				buf.append(cmd);
			}
			
			return SSHClient.exec(conn, buf.toString() ,timeout,needOutput);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 本地文件远程上传
	 */
	public static void uploadFile(String localFilePath, String remotePath,SSHConfig conf)throws Exception{
		Connection conn = SSHClient.connect(conf.getIp(),conf.getPort(), conf.getName(), conf.getPassword());
		try {
			uploadFile(localFilePath, remotePath, conn);
		} catch (Exception e) {
			throw e;
		}finally{
			try {
				conn.close();
			} catch (Exception e2) {}
		}
	}
	
	/**
	 * 本地文件远程上传
	 */
	public static void uploadFile(String localFilePath, String remotePath,Connection conn)throws Exception{
		if(conn == null){
			throw new Exception("连接失败");
		}
		try {
			SSHClient.upload(conn, localFilePath, remotePath);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static String grepPid(String keywords,int timeout,Connection conn) throws Exception{
		String pid = SSHClient.exec(conn,"ps -ef | grep "+keywords+" | grep -v grep | head -n 1 | awk '{print $2}'",timeout,true);
		pid = pid !=null?pid.trim():"";
		return pid;
	}
}
