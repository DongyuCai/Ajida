/*package com.ajida;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.axe.util.StringUtil;

import com.ajida.SSHConfig;
import com.ajida.SSHUtil;

import ch.ethz.ssh2.Connection;

public class Ssh {
	public static void main(String[] args) {
		Connection conn = null;
		try {
			SSHConfig cfg = new SSHConfig("192.168.199.45", "root", "ybsl1234");
			conn = SSHUtil.connect(cfg);
			System.out.println("connect success");
			Scanner sc = new Scanner(System.in);
			List<String> cmdContent = new ArrayList<>();
			while(true){
				String cmd = sc.nextLine().trim();
				if("exit".equalsIgnoreCase(cmd)){
					break;
				}
				if(StringUtil.isNotEmpty(cmd)){
					cmdContent.add(cmd);
					try {
						String exec = SSHUtil.exec(conn,cmdContent,10,true);
						System.out.println(exec);
						if(!cmd.toLowerCase().startsWith("cd ")){
							cmdContent.remove(cmdContent.size()-1);
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
						if(cmdContent.size() > 0){
							cmdContent.remove(cmdContent.size()-1);
						}
					}
				}
			}
			
			sc.close();
		} catch (Exception e) {
			LogUtil.error(e);
		} finally {
			try {
				if(conn != null){
					conn.close();
				}
			} catch (Exception e2) {}
		}
	}
}
*/