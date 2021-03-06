package com.ajida;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.axe.util.LogUtil;

public class CmdUtil {
	public static void main(String[] args) {
		try {
			exec(new String[]{
					"cd D:\\1-develop\\1-tool\\1-git\\2-repo\\xiangjiaoping-java\\xjp-user",
					"D:",
					"mvn clean package"
			});
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}
	
	public static String exec(String[] commands)throws Exception {
		InputStream inputStream = null;
		InputStream errorStream = null;
		StringBuilder buf = new StringBuilder();
		try {
			buf.append("cmd /c ");
			for(String cmd:commands){
				buf.append(cmd).append("&&");
			}
			buf.append("exit");
			
			Process pop = Runtime.getRuntime().exec(buf.toString());
	        // 获取其正常的输出流
			inputStream = pop.getInputStream();
	        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
	        BufferedReader br = new BufferedReader(inputStreamReader);
	        String line = null;
	        buf.setLength(0);
	        while ((line = br.readLine()) != null) {
	        	LogUtil.log(line);
	        	buf.append(line).append("\r\n");
	        }

	        // 获取其错误的输出流
	        errorStream = pop.getErrorStream();
	        InputStreamReader errorStreamReader = new InputStreamReader(errorStream, "GBK");
	        BufferedReader errorBr = new BufferedReader(errorStreamReader);
	        String errorLine = null;
	        boolean hasError = false;
	        while ((errorLine = errorBr.readLine()) != null) {
	        	hasError = true;
	        	LogUtil.log(errorLine);
	        }
	        if(hasError){
	        	if(buf.indexOf("KB/s")<0){
	        		throw new Exception(buf.toString());
	        	}
	        }
		} catch (Exception e) {
			throw e;
		} finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (Exception e2) {}
			}
			if(errorStream != null){
				try {
					errorStream.close();
				} catch (Exception e2) {}
			}
		}

//        pop.waitFor();
        return buf.toString();
    }

}
