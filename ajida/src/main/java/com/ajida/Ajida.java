package com.ajida;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.axe.util.FileUtil;
import org.axe.util.StringUtil;

import com.ajida.util.Client;
import com.ajida.util.ZipUtil;

import ch.ethz.ssh2.Connection;

public class Ajida {
	
	public static void update(String resourceDir, String targetDir, String project, String linuxIp, String linuxUsername, String linuxPassword, String tomcatDir, boolean restartTomcat) throws Exception{
		//拷贝配置文件
		String[] resourceFileList = new File(resourceDir).list();
		for(String rf:resourceFileList){
			FileUtil.copy(resourceDir+"\\"+rf, targetDir+project+"\\WEB-INF\\classes");
			System.out.println("成功拷贝"+rf);
		}
		
		ZipUtil.compressDir(new File(targetDir+project), new File(targetDir+project+".war"));
		//建立远程连接
		Connection conn = Client.connect(linuxIp, linuxUsername, linuxPassword);
		//备份远程文件
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd#HH_mm_ss");
		String backupFileName = project.substring(1)+".war_"+sdf.format(new Date());
		Client.exec(conn, "cp -n "+tomcatDir+"/webapps/"+project.substring(1)+".war "+tomcatDir+"/webapps_backup/"+backupFileName+".war ", 10);
		System.out.println("备份"+tomcatDir+"/webapps/"+project.substring(1)+".war "+tomcatDir+"/webapps_backup/"+backupFileName+".war ");
		//获取tomcat pid并杀掉
		String pid = Client.exec(conn, "ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'" ,20);
		pid = pid !=null?pid.trim():"";
		while(StringUtil.isNotEmpty(pid)){
			System.out.println("停止tomcat进程"+pid);
			Client.exec(conn, "kill -9 "+pid ,10);
			pid = Client.exec(conn, "ps -ef | grep tomcat | grep java | grep -v grep | awk '{print $2}'" ,20);
			pid = pid !=null?pid.trim():"";
		}
		//上传war包
		Client.upload(conn, targetDir+project+".war", tomcatDir+"/webapps");
		if(restartTomcat){
			//启动tomcat
			System.out.println("重启tomcat");
			Client.exec(conn, tomcatDir+"/bin/startup.sh" ,20);
		}
		conn.close();
	}
	
}
