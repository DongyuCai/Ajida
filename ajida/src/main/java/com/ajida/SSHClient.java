package com.ajida;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.axe.util.LogUtil;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHClient {

	/**
	 * 远程连接
	 * 
	 * @param ip
	 *            远程服务器地址
	 * @param usr
	 *            用户名
	 * @param psword
	 *            密码
	 * @return 连接
	 */
	public static Connection connect(String ip,int port, String usr, String psword)throws Exception {
		// 创建远程连接，默认连接端口为22，如果不使用默认，可以使用方法
		// new Connection(ip, port)创建对象
		Connection conn = new Connection(ip, port);
		// 连接远程服务器
		conn.connect();
		// 使用用户名和密码登录
		if (conn.authenticateWithPassword(usr, psword)) {
			return conn;
		}
		return null;
	}

	/**
	 * 上传本地文件到服务器目录下
	 * 
	 * @param conn
	 *            Connection对象
	 * @param fileName
	 *            本地文件
	 * @param remotePath
	 *            服务器目录
	 */
	public static void upload(Connection conn, String fileName, String remotePath)throws Exception {
		SCPClient sc = new SCPClient(conn);
		sc.put(fileName, remotePath);
	}

	/**
	 * 下载服务器文件到本地目录
	 * 
	 * @param fileName
	 *            服务器文件
	 * @param localPath
	 *            本地目录
	 */
	public static void download(Connection conn, String fileName, String localPath) {
		SCPClient sc = new SCPClient(conn);
		try {
			sc.get(fileName, localPath);
		} catch (IOException e) {
			LogUtil.error(e);
		}
	}

	/**
	 * 在远程LINUX服务器上，在指定目录下，删除指定文件
	 * 
	 * @param[in] fileName 文件名
	 * @param[in] remotePath 远程主机的指定目录
	 * @return
	 */
	public static void remoteDelete(Connection conn, String remotePath, String fileName) {
		try {
			SFTPv3Client sft = new SFTPv3Client(conn);
			// 获取远程目录下文件列表
			Vector<?> v = sft.ls(remotePath);

			for (int i = 0; i < v.size(); i++) {
				SFTPv3DirectoryEntry s = new SFTPv3DirectoryEntry();
				s = (SFTPv3DirectoryEntry) v.get(i);
				// 判断列表中文件是否与指定文件名相同
				if (s.filename.equals(fileName)) {
					// rm()方法中，须是文件绝对路径+文件名称
					sft.rm(remotePath + s.filename);
				}
				sft.close();
			}
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}

	/**
	 * 执行脚本
	 * 
	 * @param conn
	 *            Connection对象
	 * @param cmds
	 *            要在linux上执行的指令
	 * @param timeout
	 *            超时秒数
	 * @throws Exception
	 */
	public static String exec(Connection conn, String cmd, int timeout, boolean needOutput) throws Exception {
		StringBuilder buf = new StringBuilder();
		BufferedReader br = null;
		BufferedReader errorBr = null;
		Session sess = null;
		try {
			sess = conn.openSession();
			// 在远程服务器上执行linux指令
			sess.execCommand(cmd);
	        boolean hasError = false;
			if(needOutput){
				// 指令执行结束后的输出
				InputStream inputStream = new StreamGobbler(sess.getStdout());
				br = new BufferedReader(new InputStreamReader(inputStream));
				String line = null;
		        buf.setLength(0);
				while ((line = br.readLine()) != null) {
		        	buf.append(line).append("\r\n");
		        }
				
				// 指令执行结束后的错误
				InputStream errorStream = new StreamGobbler(sess.getStderr());
				errorBr = new BufferedReader(new InputStreamReader(errorStream));
		        String errorLine = null;
		        while ((errorLine = errorBr.readLine()) != null) {
		        	buf.append(errorLine).append("\r\n");
		        	hasError = true;
		        }
			}
			// 等待指令执行结束，毫秒
	        sess.waitForCondition(ChannelCondition.EXIT_STATUS, timeout * 1000);
			// 取得指令执行结束后的状态
//			LogUtil(session.getExitStatus());
	        if(hasError){
	        	throw new Exception(buf.toString());
	        }
		} catch (Exception e) {
			throw e;
		} finally {
			if(br != null){
				try {
					br.close();
				} catch (Exception e2) {}
			}
			if(errorBr != null){
				try {
					errorBr.close();
				} catch (Exception e2) {}
			}
			if(sess != null){
				try {
					sess.close();
				} catch (Exception e2) {}
			}
		}

		return buf.toString();
	}
}
