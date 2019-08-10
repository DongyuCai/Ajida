package com.ajida;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.axe.util.StringUtil;

/**
 * @description 将指定的文件压缩成Zip包
 * @author zyg
 */
public class ZipUtil {
	/**
	 * 压缩文件夹
	 * 
	 * @param zipFileName
	 *            压缩后的文件
	 * @param sourceFile
	 *            未压缩的文件夹
	 * @throws Exception
	 */
	public static void compressDir(File sourceFile,String zipFileName)throws Exception {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFileName));
			compressDir(out, sourceFile, "");
		} catch (Exception e) {
			throw e;
		} finally {
			if(out != null){
				try {
					out.close();
				} catch (Exception e2) {}
			}
		}
		
	}

	/**
	 * 压缩
	 * 
	 * @param sourceFile
	 *            未压缩的文件夹路径
	 * @param base
	 *            父级文件夹名称
	 * @return
	 */
	private static void compressDir(ZipOutputStream out, File sourceFile, String base) throws Exception {
		if (sourceFile.isDirectory()) {
			// 判断是否为目录
			File[] fl = sourceFile.listFiles();
			if(StringUtil.isNotEmpty(base)){
				out.putNextEntry(new ZipEntry(base + "/")); // 创建了一个父条目并将流指定到这一个条目的开始处
			}
			base = base.length() == 0 ? "" : base + "/"; // 这个判断base是否存在，如果存在带到下一级目录共同创建下下一级条目
			for (int i = 0; i < fl.length; i++) {
				compressDir(out, fl[i], base + fl[i].getName());
			}

		} else {
			// 压缩目录中的所有文件
			out.putNextEntry(new ZipEntry(base));
			FileInputStream in = new FileInputStream(sourceFile);
			Logger.log("compress:"+base);
			byte[] b = new byte[1024 * 1024];
			int len = 0;
			while ((len = in.read(b)) != -1) {
				out.write(b, 0, len);
				out.flush();
			}
			in.close();

		}
	}

	public static void main(String[] args) {
		try {
			compressDir(new File("D:\\tmp\\test"),"D:\\tmp\\xjp-user.war");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}