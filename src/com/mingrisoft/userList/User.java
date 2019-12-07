package com.mingrisoft.userList;

import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * 用户类
 *
 */
public class User {
	private String ip;// 用户IP
	private String host;// 用户地址名称
	private String tipText;// 提示内容
	private String name;// 用户名
	private String icon;// 用户头像地址
	private Icon iconImg = null;// 用户头像图标

	/**
	 * 空构造方法
	 */
	public User() {

	}

	public User(String host, String ip) {
		this.ip = ip;
		this.host = host;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String toString() {
		String strName = getName() == null ? getHost() : getName();
		return strName;
	}

	public String getIcon() {
		return icon;
	}

	/**
	 * 获取头像图片
	 * 
	 * @return 头像图标对象
	 */
	public Icon getIconImg() {
		int faceNum = 1;// 头像编号，初始值为1
		if (ip != null && !ip.isEmpty()) {
			String[] num = ip.split("\\.");// 按照“.”分割字符串
			if (num.length == 4) {// 如果是正常的4段数字组成的IP
				Integer num1 = Integer.parseInt(num[2]) + 1;// 获取第二段数字+1
				Integer num2 = Integer.parseInt(num[3]);// 获取第三段数字
				faceNum = (num1 * num2) % 11 + 1;// 两段数组通过公式计算得出头像值
			}
		}
		File imageFile = new File("res/NEWFACE/" + faceNum + ".png");// 创建对象的头像文件对象
		if (!imageFile.exists()) {// 如果文件不存在
			imageFile = new File("res/NEWFACE/1.png");// 使用默认头像
		}
		iconImg = new ImageIcon(imageFile.getAbsolutePath());// 此用户使用存在的头像
		return iconImg;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getTipText() {
		return tipText;
	}

	public void setTipText(String tipText) {
		this.tipText = tipText;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
