package com.mingrisoft.frame;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.mingrisoft.userList.ChatLog;
import com.mingrisoft.userList.User;

/**
 * 聊天记录对话框
 *
 */
public class ChatDialog extends JDialog {
	/**
	 * 构造方法
	 * 
	 * @param owner
	 *            - 展示在哪个窗口智商
	 * @param user
	 *            - 展示哪个用户的俩天记录
	 */
	public ChatDialog(Frame owner, User user) {
		super(owner, true);// 调用父类构造方法
		int x = owner.getX();// 获取父窗体坐标
		int y = owner.getY();
		setBounds(x + 20, y + 20, 400, 350);// 设定对话框坐标和大小
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);// 点击叉按钮销毁窗体
		setTitle("与『" + user + "』 消息记录");// 窗体标题

		JTextArea area = new JTextArea();// 显示内容的文本域
		area.setEditable(false);// 不可编辑
		area.setLineWrap(true);// 自动换行
		area.setWrapStyleWord(true);// 激活断行不断字

		List<String> logs = ChatLog.readAllLog(user.getIp());// 获取此用户的消息记录文件
		if (logs.size() == 0) {// 如果没有任何消息
			area.append("(无)");// 显示无
		} else {// 如有有消息
			for (String log : logs) {// 遍历消息集合
				area.append(log + "\n");// 逐行打印
			}
		}

		JScrollPane scro = new JScrollPane(area);// 创建文本域的滚动面板
		scro.doLayout();// 滚动条重新布置组件，使滑块可以正确判断最底部位置
		JScrollBar scroBar = scro.getVerticalScrollBar();// 获取垂直滑块
		scroBar.setValue(scroBar.getMaximum());// 滚动条滑到底

		JPanel mainPanel = new JPanel();// 创建主容器面板
		mainPanel.setLayout(new BorderLayout());// 主容器采用边界布局
		mainPanel.add(scro, BorderLayout.CENTER);// 滚动面板放入主容器中间
		setContentPane(mainPanel);// 将主容器面板放入主容器中

		setResizable(false);// 窗体不可改变
		setVisible(true);// 显示窗体
	}
}