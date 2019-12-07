package com.mingrisoft.frame;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.*;

import com.mingrisoft.EQ;
import com.mingrisoft.dao.Dao;
import com.mingrisoft.userList.ChatLog;
import com.mingrisoft.userList.ChatTree;
import com.mingrisoft.userList.User;

/**
 * 聊天窗口
 *
 */

public class TelFrame extends JFrame {
	private Dao dao;// 数据库接口
	private User user;// 对方用户类
	private JTextPane receiveText = new JTextPane();// 聊天记录文本域
	private JTextPane sendText = new JTextPane();// 发送输入框
	private JButton sendButton = new JButton();// 发送按钮
	private final JButton messageButton = new JButton();// 消息记录

	private final static Map<String, TelFrame> instance = new HashMap<String, TelFrame>();
	private JToolBar toolBar = new JToolBar();// 工具栏
	private JToggleButton toolFontButton = new JToggleButton();// 更换字体
	private JButton toolFaceButton = new JButton();// 使用表情
	private JButton toolbarSendFile = new JButton();// 发送文件按钮
	private JButton toolbarShakeFrame = new JButton();// 选择发动抖动窗口
	private JButton toolbarCaptureScreen = new JButton();// 截图
	private final JButton hideBtn = new JButton();// 隐藏侧边栏按钮

	private JLabel otherSideInfo = new JLabel();// 对方信息标签
	// private final JScrollPane infoScrollPane = new JScrollPane();
	private final JLabel label_1 = new JLabel();
	private JPanel panel_3 = new JPanel();// 右侧好友信息面板
	private byte[] buf;// 数据包内容
	private DatagramSocket ss;// UDP套接字
	private String ip;// 对方IP地址
	private DatagramPacket dp;// 数据报
	private TelFrame frame;// 聊天窗口对象
	private ChatTree tree;// 用户数对象
	private int rightPanelWidth = 148;

	private final String SHAKING = "c)3a^1]g0";// 抖动窗口命令

	/**
	 * 打开聊天窗口
	 * 
	 * @param ssArg
	 *            - UDP数据包
	 * @param dp
	 *            - UDP套接字
	 * @param treeArg
	 *            - 用户树
	 * @return
	 */
	public static synchronized TelFrame getInstance(DatagramSocket ssArg,
			DatagramPacket dp, ChatTree treeArg) {
		InetAddress packetAddress = dp.getAddress();// 获取数据包中的地址
		String tmpIp = packetAddress.getHostAddress();// 获取地址中的IP字符串
		TelFrame frame;// 创建聊天窗口对象
		if (!instance.containsKey(tmpIp)) {// 如果不存在此IP地址
			frame = new TelFrame(ssArg, dp, treeArg);// 创建新的聊天窗口
			instance.put(tmpIp, frame);// 记录此IP地址对应的聊天窗口
		} else {
			frame = instance.get(tmpIp);// 获取此IP对应的聊天窗口
			frame.setBufs(dp.getData());// 窗口载入UDP套接字传来的信息
		}
		frame.receiveInfo();// 从UDP套接字数据包中获取聊天数据
		if (!frame.isVisible()) {// 如果窗体不可见
			frame.setVisible(true);// 设为窗体可见
		}
		frame.setState(JFrame.NORMAL);// 窗口状态为非图表化
		frame.toFront();// 窗体最前端显示
		return frame;
	}

	/**
	 * 本类构造方法
	 * 
	 * @param ssArg
	 *            - UDP数据包
	 * @param dpArg
	 *            - UDP套接字
	 * @param treeArg
	 *            - 用户树
	 */
	private TelFrame(DatagramSocket ssArg, DatagramPacket dpArg,
			final ChatTree treeArg) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.tree = treeArg;
		ip = dpArg.getAddress().getHostAddress();//获得数据包从哪个IP发来的
		dao = Dao.getDao();//获取数据库接口
		user = dao.getUser(ip);
		frame = this;
		ss = ssArg;//载入UDP套接字
		dp = dpArg;// 载入数据包
		buf = dp.getData();// 获取UDP套接字发来的消息
		try {
			setBounds(200, 100, 521, 424);
			JSplitPane splitPane = new JSplitPane();
			JScrollPane scrollPane = new JScrollPane();
			getContentPane().add(splitPane);
			splitPane.setDividerSize(2);
			splitPane.setResizeWeight(0.8);
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane.setLeftComponent(scrollPane);
			scrollPane.setViewportView(receiveText);
			receiveText.setFont(new Font("宋体", Font.PLAIN, 12));
			receiveText.setInheritsPopupMenu(true);
			receiveText.setVerifyInputWhenFocusTarget(false);
			receiveText.setDragEnabled(true);
			receiveText.setMargin(new Insets(0, 0, 0, 0));
			receiveText.setEditable(false);
			receiveText.addComponentListener(new ComponentAdapter() {
				public void componentResized(final ComponentEvent e) {
					scrollPane.getVerticalScrollBar().setValue(
							receiveText.getHeight());
				}
			});
			receiveText.setDoubleBuffered(true);

			JPanel receiveTextPanel = new JPanel();

			splitPane.setRightComponent(receiveTextPanel);
			receiveTextPanel.setLayout(new BorderLayout());

			final FlowLayout flowLayout = new FlowLayout();
			flowLayout.setHgap(4);
			flowLayout.setAlignment(FlowLayout.LEFT);
			flowLayout.setVgap(0);
			JPanel buttonPanel = new JPanel();
			receiveTextPanel.add(buttonPanel, BorderLayout.SOUTH);
			final FlowLayout flowLayout_1 = new FlowLayout();
			flowLayout_1.setVgap(3);
			flowLayout_1.setHgap(20);
			buttonPanel.setLayout(flowLayout_1);

			buttonPanel.add(sendButton);
			sendButton.setMargin(new Insets(0, 14, 0, 14));
			sendButton.addActionListener(new sendActionListener());
			sendButton.setText("发送");

			buttonPanel.add(messageButton);
			messageButton.setMargin(new Insets(0, 14, 0, 14));
			messageButton.addActionListener(new MessageButtonActionListener());
			messageButton.setText("消息记录");

			JPanel toolbarPanel = new JPanel();
			receiveTextPanel.add(toolbarPanel, BorderLayout.NORTH);
			toolbarPanel.setLayout(new BorderLayout());

			ToolbarActionListener toolListener = new ToolbarActionListener();
			toolbarPanel.add(toolBar);
			toolBar.setBorder(new BevelBorder(BevelBorder.RAISED));
			toolBar.setFloatable(false);
			toolBar.add(toolFontButton);
			toolFontButton.addActionListener(toolListener);
			toolFontButton.setFocusPainted(false);
			toolFontButton.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon toolbarFontIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarFont.png"));
			toolFontButton.setIcon(toolbarFontIcon);
			toolFontButton.setToolTipText("设置字体颜色和格式");
			toolBar.add(toolFaceButton);
			toolFaceButton.addActionListener(toolListener);
			toolFaceButton.setToolTipText("选择表情");
			toolFaceButton.setFocusPainted(false);
			toolFaceButton.setMargin(new Insets(0, 0, 0, 0));

			ImageIcon toolbarFaceIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarFace.png"));
			toolFaceButton.setIcon(toolbarFaceIcon);
			toolBar.add(toolbarSendFile);

			toolbarSendFile.addActionListener(toolListener);
			toolbarSendFile.setToolTipText("发送文件");
			toolbarSendFile.setFocusPainted(false);
			toolbarSendFile.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon toolbarPictureIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarPicture.png"));
			toolbarSendFile.setIcon(toolbarPictureIcon);

			toolBar.add(toolbarShakeFrame);
			toolbarShakeFrame.setActionCommand("shaking");// 添加按钮动作指令为“抖动”
			toolbarShakeFrame.addActionListener(toolListener);
			toolbarShakeFrame.setToolTipText("发送窗口抖动");
			toolbarShakeFrame.setFocusPainted(false);
			toolbarShakeFrame.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon toolbarShakeIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarShake.png"));
			 toolbarShakeFrame.setIcon(toolbarShakeIcon);

			toolbarCaptureScreen.setActionCommand("CaptureScreen");// 添加按钮动作指令为“抖动”
			toolbarCaptureScreen.addActionListener(toolListener);
			toolbarCaptureScreen.setToolTipText("截图");
			toolbarCaptureScreen.setFocusPainted(false);
			toolbarCaptureScreen.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon toolbarCaptureScreenIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarSceneCaptureScreen.png"));
			 toolbarCaptureScreen.setIcon(toolbarCaptureScreenIcon);
			toolBar.add(toolbarCaptureScreen);

			JScrollPane scrollPane_1 = new JScrollPane();
			toolbarPanel.add(hideBtn, BorderLayout.EAST);
			hideBtn.addActionListener(new hideBtnActionListener());
			hideBtn.setMargin(new Insets(0, 0, 0, 0));
			hideBtn.setText(">");

			JPanel sendTextPanel = new JPanel();
			receiveTextPanel.add(sendTextPanel);
			sendTextPanel.setLayout(new BorderLayout());
			sendTextPanel.add(scrollPane_1);
			scrollPane_1
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sendText.setInheritsPopupMenu(true);
			sendText.addKeyListener(new SendTextKeyListener());
			sendText.setVerifyInputWhenFocusTarget(false);
			sendText.setFont(new Font("宋体", Font.PLAIN, 12));
			sendText.setMargin(new Insets(0, 0, 0, 0));
			sendText.setDragEnabled(true);// 文本可拖动
			sendText.requestFocus();// 获取焦点
			scrollPane_1.setViewportView(sendText);

			addWindowListener(new TelFrameClosing(tree));

			JScrollPane infoScrollPane = new JScrollPane();
			add(panel_3, BorderLayout.EAST);
			panel_3.setLayout(new BorderLayout());
			panel_3.add(infoScrollPane);
			infoScrollPane
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			infoScrollPane.setViewportView(otherSideInfo);
			otherSideInfo.setIconTextGap(-1);// 图标和文本之间的间隔为-1像素
			String imgPath = EQ.class
					.getResource("/image/telFrameImage/telUserInfo.png") + "";
			// 标签使用HTML超文本格式显示对方信息
			otherSideInfo.setText("<html><body background='" + imgPath
					+ "'><table width='" + rightPanelWidth
					+ "'><tr><td>用户名：<br>&nbsp;&nbsp;" + user.getName()
					+ "</td></tr><tr><td>主机名：<br>&nbsp;&nbsp;" + user.getHost()
					+ "</td></tr>" + "<tr><td>IP地址：<br>&nbsp;&nbsp;"
					+ user.getIp() + "</td></tr><tr><td colspan='2' height="
					+ this.getHeight() * 2
					+ "></td></tr></table></body></html>");

			panel_3.add(label_1, BorderLayout.NORTH);
			label_1.setIcon(new ImageIcon(EQ.class
					.getResource("/image/telFrameImage/telUserImage.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setVisible(true);
		setTitle("与『" + user + "』通讯中");
	}

	/**
	 * 让窗口抖动
	 */
	private void shaking() {
		int x = getX();// 获取窗体横坐标
		int y = getY();// 获取窗体纵坐标
		for (int i = 0; i < 10; i++) {// 循环十次
			if (i % 2 == 0) {// 如果i为偶数
				x += 5;// 横坐标加5
				y += 5;// 纵坐标加5
			} else {
				x -= 5;// 横坐标减5
				y -= 5;// 纵坐标减5
			}
			setLocation(x, y);// 重新设置窗体位置
			try {
				Thread.sleep(50);// 休眠50毫秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取聊天数据，并添加到聊天记录面板中
	 * 
	 */
	private void receiveInfo() {
		if (buf.length > 0) {
			String rText = new String(buf).replace("" + (char) 0, "");// 将UDP套接字获取的数据转为字符串
			String hostAddress = dp.getAddress().getHostAddress();// 获取发送数据包的IP对象
			String info = dao.getUser(hostAddress).getName();// 获取此IP地址对应的用户名
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 创建日期格式化类
			info = info + "  (" + sdf.format(new Date()) + ")";// 在消息后面添加日期
			appendReceiveText(info, Color.BLUE);// 将用户信息追加到聊天面板中
			if (rText.equals(SHAKING)) {
				appendReceiveText("[对方发送了一个抖动窗口]\n", Color.RED);// 将用户发送的消息追加到聊天面板中
				shaking();// 让自己的窗口抖动
			} else {
				appendReceiveText(rText + "\n", null);// 将用户发送的消息追加到聊天面板中
				ChatLog.writeLog(user.getIp(), info);// 记录发送的聊天记录(用户名)
				ChatLog.writeLog(user.getIp(), rText);// 记录发送的聊天记录(消息)
			}

		}
	}

	/**
	 * 发送按钮触发事件
	 *
	 */
	class sendActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			String sendInfo = getSendInfo();// 获取要发送的消息
			if (sendInfo == null)// 如果消息为空
				return;
			insertUserInfoToReceiveText();// 聊天记录窗口插入当前用户名
			appendReceiveText(sendInfo + "\n", null);// 聊天记录添加无颜色文本
			byte[] tmpBuf = sendInfo.getBytes();// 将字符串变为字节数组
			DatagramPacket tdp = null;// 创建UDP数据包
			try {
				// 初始化数据包，参数：数据数组，数组长度，要发送的地址
				tdp = new DatagramPacket(tmpBuf, tmpBuf.length,
						new InetSocketAddress(ip, 1111));
				ss.send(tdp);// UDP套接字发送数据包
				ChatLog.writeLog(user.getIp(), sendInfo);// 记录发送的聊天记录（聊天信息）（用户名记录在insertUserInfoToReceiveText()方法中）
			} catch (SocketException e2) {
				e2.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(TelFrame.this, e1.getMessage());
			}
			sendText.setText(null);// 清空输入框内容
			sendText.requestFocus();// 输入框获得焦点
		}
	}

	/**
	 * 功能栏触发监听
	 *
	 */
	class ToolbarActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			String command = e.getActionCommand();// 获取按钮动作指令
			switch (command) {// 判断按钮动作指令
			case "shaking":// 如果按钮指令为抖动
				insertUserInfoToReceiveText();// 聊天记录窗口插入当前用户名
				appendReceiveText("[您发送了一个抖动窗口，3秒之后可再次发送]\n", Color.GRAY);// 聊天记录添加无颜色文本
				ChatLog.writeLog(user.getIp(), "[发送窗体抖动命令]");// 记录发送的聊天记录(用户)
				sendShakeCommand(e);// 发送抖动指令
				break;
			case "CaptureScreen":
				new CaptureScreenUtil();
				break;
			default:
				JOptionPane.showMessageDialog(TelFrame.this, "此功能尚在建设中。");
			}
		}
	}

	/**
	 * 发送窗体抖动指令
	 * 
	 * @param e
	 *            - 触发抖动指令的组件
	 */
	private void sendShakeCommand(ActionEvent e) {
		Thread t = new Thread() {// 创建匿名线程内部类
			public void run() {// 重写run方法
				Component c = (Component) e.getSource();// 获取触发抖动指令的组件
				try {
					byte[] tmpBuf = SHAKING.getBytes();// 将抖动命令变为字节数组
					// 创建UDP数据包。 初始化数据包，参数：数据数组，数组长度，要发送的地址
					DatagramPacket tdp = new DatagramPacket(tmpBuf,
							tmpBuf.length, new InetSocketAddress(ip, 1111));
					ss.send(tdp);// UDP套接字发送数据包;
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					c.setEnabled(false);// 禁止使用触发抖动指令的组件
					try {
						Thread.sleep(3000);// 3秒之后
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} finally {
						c.setEnabled(true);// 触发抖动指令的组件回复可使用状态
					}
				}
			};
		};
		t.start();// 开启线程
	}

	/**
	 * 窗体关闭事件
	 *
	 */
	private final class TelFrameClosing extends WindowAdapter {
		private final JTree tree;

		private TelFrameClosing(JTree tree) {
			this.tree = tree;
		}

		public void windowClosing(final WindowEvent e) {
			tree.setSelectionPath(null);// 清空用户数选中状态
			TelFrame.this.setState(ICONIFIED);// 窗体图标化
			TelFrame.this.dispose();// 窗体销毁
		}
	}

	/**
	 * 消息记录按钮动作事件
	 *
	 */
	private class MessageButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			new ChatDialog(frame, user);// 打开消息记录窗口
		}
	}

	/**
	 * 快捷键键盘监听类
	 */
	private class SendTextKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {// 如果按下“Ctrl+Enter”组合键
				sendButton.doClick();// 则触发发送按钮功能
			}
		}
	}

	/**
	 * 隐藏好友信息面板事件
	 *
	 */
	private class hideBtnActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (">".equals(hideBtn.getText())) {// 如果按钮显示>
				hideBtn.setText("<");// 让按钮显示<
			} else {
				hideBtn.setText(">");// 让按钮显示>
			}
			panel_3.setVisible(!panel_3.isVisible());// 将好友信息面板的隐藏状态改为相反的状态
			TelFrame.this.setVisible(true);// 主面板始终不隐藏
		}
	}


	/**
	 * 设置窗体读取的消息数据
	 * 
	 * @param bufs
	 *            - 消息数据
	 */
	public void setBufs(byte[] bufs) {
		this.buf = bufs;
	}

	/**
	 * 获取要发送的消息
	 * 
	 * @return 要发送的消息
	 */
	public String getSendInfo() {
		String sendInfo = "";
		Document doc = sendText.getDocument();// 获取输入框的文本对象
		try {
			sendInfo = doc.getText(0, doc.getLength());// 提取文本对象的字符串内容
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		if (sendInfo.equals("")) {// 如果内容为空
			JOptionPane.showMessageDialog(TelFrame.this, "不能发送空信息。");
			return null;
		}
		return sendInfo;
	}

	/**
	 * 聊天记录窗口插入当前用户名
	 */
	private void insertUserInfoToReceiveText() {
		String info = null;
		try {
			String hostAddress = InetAddress.getLocalHost().getHostAddress();// 获取本地地址名称
			info = dao.getUser(hostAddress).getName();// 查找此地址名称对应的用户名
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 创建日期格式化类
		info = info + "  (" + sdf.format(new Date()) + ")";// 在消息后面添加日期
		appendReceiveText(info, new Color(68, 184, 29));// 聊天记录添加绿颜色文本
		ChatLog.writeLog(user.getIp(), info);// 记录发送的聊天记录(用户)
	}

	public JTextPane getSendText() {
		return sendText;
	}

	/**
	 * 聊天记录框追加文本
	 * 
	 * @param sendInfo
	 *            - 追加文本内容
	 * @param color
	 *            - 文本颜色
	 */
	public void appendReceiveText(String sendInfo, Color color) {
		Style style = receiveText.addStyle("title", null);// 使用title字体
		if (color != null) {
			StyleConstants.setForeground(style, color);
		} else {
			StyleConstants.setForeground(style, Color.BLACK);
		}
		receiveText.setEditable(true);// 聊天记录框为可编辑状态
		receiveText.setCaretPosition(receiveText.getDocument().getLength());// 设定文本插入位置为现有内容的底部
		receiveText.setCharacterAttributes(style, false);// 将内容的样式改为style样式，不改变全局
		receiveText.replaceSelection(sendInfo + "\n");// 在插入点位置替换文本内容
		receiveText.setEditable(false);// 聊天记录框为不可编辑状态
	}
}
