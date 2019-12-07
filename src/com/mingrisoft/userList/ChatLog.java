package com.mingrisoft.userList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * 聊天记录读写工具类
 *
 */
public class ChatLog {

	/**
	 * 记录聊天日志
	 * 
	 * @param userIP
	 *            - 对方IP地址
	 * @param message
	 *            - 记录的消息
	 */
	static public void writeLog(String userIP, String message) {
		File rootdir = new File("db_EQ/chatdata/");// 创建数据库目录下子目录对象
		if (!rootdir.exists()) {// 如果这个文件夹不存在
			rootdir.mkdirs();// 创建这个文件夹
		}
		File log = new File(rootdir, userIP + ".chat");// 创建日志文件对象，命名为“用户IP.chat”
		if (!log.exists()) {// 如果文件不存在
			try {
				log.createNewFile();// 创建空文件
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("聊天记录文件无法创建：" + log.getAbsolutePath());
			}
		}
		// 利用try语句块创建IO流对象
		try (FileOutputStream fos = new FileOutputStream(log, true);// 创建文件输出字节流，可在源文件之后追加新内容
				OutputStreamWriter os = new OutputStreamWriter(fos);// 将字节流转为字符流
				BufferedWriter bw = new BufferedWriter(os);// 创建缓冲字符流
		) {
			bw.write(message);// 写入日志信息
			bw.newLine();// 添加一个新行
			bw.flush();// 字符流刷新
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取聊天记录
	 * 
	 * @param userIP
	 *            - 读取与哪一个IP相关的聊天记录
	 * @return - 聊天记录集合
	 */
	static public List<String> readAllLog(String userIP) {
		List<String> logs = new LinkedList<String>();// 创建记录集合
		File rootdir = new File("db_EQ/chatdata/");// 创建数据库目录下子目录对象
		if (!rootdir.exists()) {// 如果这个文件夹不存在
			rootdir.mkdirs();// 创建这个文件夹
		}
		File log = new File(rootdir, userIP + ".chat");// 创建日志文件对象，命名为“用户IP.chat”
		if (!log.exists()) {// 如果文件不存在
			return logs;// 返回空集合
		}
		// 利用try语句块创建IO流对象
		try (FileInputStream fis = new FileInputStream(log);// 创建文件输入字节流
				InputStreamReader is = new InputStreamReader(fis);// 将字节流转为字符流
				BufferedReader br = new BufferedReader(is);// 创建缓冲字符流
		) {
			String oneLine = null;// 创建缓冲读取的临时字符串
			while ((oneLine = br.readLine()) != null) {// 读取字符流中一行内容，如果这一行内容不为null的话
				logs.add(oneLine);// 消息集合添加此记录
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logs;// 返回消息集合
	}
}
