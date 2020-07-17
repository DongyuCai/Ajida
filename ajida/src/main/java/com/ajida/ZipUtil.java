package com.ajida;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.axe.util.LogUtil;
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
	public static void zip(File sourceFile, String zipFileName, Set<String> excludes) throws Exception {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFileName));
			zip(out, sourceFile, "", excludes);
		} catch (Exception e) {
			throw e;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e2) {
				}
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
	private static void zip(ZipOutputStream out, File sourceFile, String base, Set<String> excludes) throws Exception {
		boolean needExclude = false;
		for (String name : excludes) {
			if (sourceFile.getName().contains(name)) {
				needExclude = true;
				break;
			}
		}
		if (!needExclude) {
			if (sourceFile.isDirectory()) {
				// 判断是否为目录
				File[] fl = sourceFile.listFiles();
				if (StringUtil.isNotEmpty(base)) {
					out.putNextEntry(new ZipEntry(base + "/")); // 创建了一个父条目并将流指定到这一个条目的开始处
				}
				base = base.length() == 0 ? "" : base + "/"; // 这个判断base是否存在，如果存在带到下一级目录共同创建下下一级条目
				for (int i = 0; i < fl.length; i++) {
					zip(out, fl[i], base + fl[i].getName(), excludes);
				}

			} else {
				// 压缩目录中的所有文件
				out.putNextEntry(new ZipEntry(base));
				FileInputStream in = new FileInputStream(sourceFile);
				LogUtil.log("compress:" + base);
				byte[] b = new byte[1024 * 1024];
				int len = 0;
				while ((len = in.read(b)) != -1) {
					out.write(b, 0, len);
					out.flush();
				}
				in.close();

			}
		}
	}

	public static void unZip(File sourceZipFile,String destDir) throws Exception {
		if (!sourceZipFile.exists()) {
			throw new Exception("file not found！" + sourceZipFile.getAbsolutePath());
		}
		if (!sourceZipFile.getName().endsWith(".zip")) {
			throw new Exception("is not zip file！" + sourceZipFile.getName());
		}
		ZipFile zipFile = new ZipFile(sourceZipFile);
		File baseDir = new File(destDir);
		byte[] buf = new byte[1024];
		for (Enumeration<ZipEntry> entries = zipFile.getEntries(); entries.hasMoreElements();) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			File file = new File(baseDir, entry.getName());
			if (entry.isDirectory()) {
				if (!file.exists()) {
					file.mkdirs();// 如果是目录且不存在就要创建
				}
			} else {
				// 不是目录就是文件
				// 判断文件的目录是否存在
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				InputStream in = null;
				OutputStream out = null;
				try {
					in = zipFile.getInputStream(entry);
					out = new FileOutputStream(file);
					int readLen = 0;
					while ((readLen = in.read(buf)) > 0) {
						out.write(buf, 0, readLen);
					}
				} catch (Exception e) {
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e2) {}
					}
					if (out != null) {
						try {
							out.close();
						} catch (Exception e2) {}
					}

				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			// zip(new File("D:\\tmp\\error"), "D:\\tmp\\error.zip", new
			// HashSet<String>());
			unZip(new File("D:\\tmp\\error.zip"),"D:\\tmp\\error_2");
		} catch (Exception e) {
			LogUtil.error(e);
		}
	}
}