package com.mingrisoft;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.mingrisoft.dao.Dao;
import com.mingrisoft.frame.TelFrame;
import com.mingrisoft.system.Resource;
import com.mingrisoft.userList.ChatTree;
import com.mingrisoft.userList.User;

public class EQ extends Dialog {
	public static EQ frame = null;// 主窗体本类对象
	private JTextField ipEndTField;// IP搜索范围结束值
	private JTextField ipStartTField;// IP搜索范围开始值
	private ChatTree chatTree;// 用户列表树
	private JPopupMenu popupMenu;// 鼠标右键菜单（弹出式菜单）
	private JTabbedPane tabbedPane;// 主标签面板
	private JToggleButton searchUserButton;//
	private JProgressBar progressBar;// 鼠标右键菜单（弹出式菜单）
	private JList faceList;// 界面风格集合
	private JButton selectInterfaceOKButton;// 确定界面效果按钮
	private DatagramSocket ss;// UDP套接字
	private final JLabel stateLabel;// 底部状态栏标签
	private Rectangle location;// 窗口位置对象
	public static TrayIcon trayicon;// 系统托盘图标
	private Dao dao;// 数据库接口
	public final static Preferences preferences = Preferences.systemRoot();// 创建首选项对象，使用系统的根首选项节点。此对象可以保存我们偏好设置
	private JButton userInfoButton;// 用户信息按钮

	public static void main(String args[]) {
		Toolkit tk=Toolkit.getDefaultToolkit();  
		Image image=tk.createImage("/icons/sysTray.png"); /*image.gif是图标*/  	 
		try {
			String laf = preferences.get("lookAndFeel", "默认");
			if (laf.contains("当前系统"))// 如果字符串包含“当前系统”字样
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());// 获取本机的窗体外观
			EQ frame = new EQ();
			frame.setVisible(true);
			frame.SystemTrayInitial();// 初始化系统栏
			frame.server();// 启动服务器
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 主类构造方法
	 */
	public EQ() {
		super(new Frame());// 调用父类方法，创建一个空父类窗体
		frame = this;
		dao = Dao.getDao();// 获取数据库接口对象
		location = dao.getLocation();// 获取数据库中的位置
		setTitle("Chat-LAN");
		setBounds(location);// 指定窗口大小即所处位置
		progressBar = new JProgressBar();
		progressBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		tabbedPane = new JTabbedPane();
		popupMenu = new JPopupMenu();
		chatTree = new ChatTree(this);
		stateLabel = new JLabel(); // 状态栏标签
		addWindowListener(new FrameWindowListener());// 添加窗体监视器
		addComponentListener(new ComponentAdapter() {// 添加组件侦听器
			public void componentResized(final ComponentEvent e) {// 窗体改变大小时
				saveLocation();// 保存主窗体位置的方法
			}

			public void componentMoved(final ComponentEvent e) {// 窗体移动时
				saveLocation();// 保存主窗体位置的方法
			}
		});
		try {
			ss = new DatagramSocket(1111);// 启动通讯服务端口
		} catch (SocketException e2) {
			if (e2.getMessage().startsWith("Address already in use"))
				JOptionPane.showMessageDialog(this, "服务端口被占用,或者本软件已经运行。");
			System.exit(0);
		}
		final JPanel BannerPanel = new JPanel();
		BannerPanel.setLayout(new BorderLayout());
		add(BannerPanel, BorderLayout.NORTH);
		userInfoButton = new JButton();
		BannerPanel.add(userInfoButton, BorderLayout.WEST);
		userInfoButton.setMargin(new Insets(0, 0, 0, 10));
		initUserInfoButton();// 初始化本地用户头像按钮

		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setTabPlacement(SwingConstants.LEFT);
		ImageIcon userTicon = new ImageIcon(EQ.class.getResource("/image/tabIcon/tabLeft.PNG"));
		tabbedPane.addTab(null, userTicon, createUserList(), "用户列表");
		ImageIcon sysOTicon = new ImageIcon(EQ.class.getResource("/image/tabIcon/tabLeft2.PNG"));
		tabbedPane.addTab(null, sysOTicon, createSysToolPanel(), "系统操作");
		setAlwaysOnTop(true);
	}

	/**
	 * 用户列表面板
	 * 
	 * @return
	 */

	private JScrollPane createUserList() {// 用户列表面板
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		addUserPopup(chatTree, getPopupMenu());// 为用户添加弹出菜单
		scrollPane.setViewportView(chatTree);// 将用户树添加到滚动面板中
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		chatTree.addMouseListener(new ChatTreeMouseListener());
		return scrollPane;
	}

	/**
	 * 系统工具面板
	 * 
	 * @return
	 */
	private JScrollPane createSysToolPanel() {
		JPanel sysToolPanel = new JPanel(); // 系统工具面板
		sysToolPanel.setLayout(new BorderLayout());
		JScrollPane sysToolScrollPanel = new JScrollPane();
		sysToolScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sysToolScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		sysToolScrollPanel.setViewportView(sysToolPanel);
		sysToolPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JPanel interfacePanel = new JPanel();
		sysToolPanel.add(interfacePanel, BorderLayout.NORTH);
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.setBorder(new TitledBorder("界面选择-再次启动生效"));
		faceList = new JList(new String[] { "当前系统", "默认" });
		interfacePanel.add(faceList);
		faceList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		final JPanel interfaceSubPanel = new JPanel();
		interfaceSubPanel.setLayout(new FlowLayout());
		interfacePanel.add(interfaceSubPanel, BorderLayout.SOUTH);
		selectInterfaceOKButton = new JButton("确定");
		selectInterfaceOKButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (faceList.getSelectedValue() == null) {
					JOptionPane.showMessageDialog(EQ.this, "您未选中任何主题", "提示", JOptionPane.ERROR_MESSAGE);
					return;
				}
				preferences.put("lookAndFeel", faceList.getSelectedValue().toString());
				JOptionPane.showMessageDialog(EQ.this, "重新运行本软件后生效");
			}
		});
		interfaceSubPanel.add(selectInterfaceOKButton);

		JPanel searchUserPanel = new JPanel(); // 用户搜索面板
		sysToolPanel.add(searchUserPanel);
		searchUserPanel.setLayout(new BorderLayout());
		final JPanel searchControlPanel = new JPanel();
		searchControlPanel.setLayout(new GridLayout(0, 1));
		searchUserPanel.add(searchControlPanel, BorderLayout.SOUTH);
		final JList searchUserList = new JList(new String[] { "检测用户列表" });// 新添加用户列表
		final JScrollPane scrollPane_2 = new JScrollPane(searchUserList);
		scrollPane_2.setDoubleBuffered(true);
		searchUserPanel.add(scrollPane_2);
		searchUserList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		searchUserButton = new JToggleButton();
		searchUserButton.setText("搜索新用户");
		searchUserButton.addActionListener(new SearchUserActionListener(searchUserList));
		searchControlPanel.add(progressBar);
		searchControlPanel.add(searchUserButton);
		searchUserPanel.setBorder(new TitledBorder("搜索用户"));

		final JPanel ipPanel = new JPanel();
		final GridLayout gridLayout_2 = new GridLayout(0, 1);
		gridLayout_2.setVgap(5);// 组件之间间距为5像素
		ipPanel.setLayout(gridLayout_2);
		ipPanel.setMaximumSize(new Dimension(600, 90));
		ipPanel.setBorder(new TitledBorder("IP搜索范围"));
		final JPanel panel_5 = new JPanel();
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
		ipPanel.add(panel_5);
		panel_5.add(new JLabel("起始IP："));
		ipStartTField = new JTextField("192.168.0.1");
		panel_5.add(ipStartTField);
		final JPanel panel_6 = new JPanel();
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));
		ipPanel.add(panel_6);
		panel_6.add(new JLabel("终止IP："));
		ipEndTField = new JTextField("192.168.1.255");
		panel_6.add(ipEndTField);
		sysToolPanel.add(ipPanel, BorderLayout.SOUTH);

		stateLabel.setText("总人数：" + chatTree.getRowCount());
		return sysToolScrollPanel;
	}

	/**
	 * 初始化用户信息按钮
	 */
	private void initUserInfoButton() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();// 获取本地地址对象
			User user = dao.getUser(ip);// 从数据库中获取本地用户资料
			userInfoButton.setIcon(user.getIconImg());
			userInfoButton.setText(user.getName());
			userInfoButton.setIconTextGap(JLabel.RIGHT);
			userInfoButton.setToolTipText(user.getTipText());
			userInfoButton.getParent().doLayout();// 父窗体重新布局
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 搜索用户事件
	 *
	 */
	class SearchUserActionListener implements ActionListener {
		private final JList list;// 添加用户列表

		SearchUserActionListener(JList list) {
			this.list = list;
		}

		public void actionPerformed(ActionEvent e) {
			// IP地址的正则表达式
			String regex = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
			String ipStart = ipStartTField.getText().trim();// 获得开始IP的地址
			String ipEnd = ipEndTField.getText().trim();// 获得结束IP的地址
			if (ipStart.matches(regex) && ipEnd.matches(regex)) {// 如果两个IP地址都符合格式要求
				if (searchUserButton.isSelected()) {// 如果按钮是选中状态
					searchUserButton.setText("停止搜索");// 按钮文本设为
					new Thread(new Runnable() {
						public void run() {
							// 搜索用户
							Resource.searchUsers(chatTree, progressBar, list, searchUserButton, ipStart, ipEnd);
						}
					}).start();// 开启线程
				} else {
					searchUserButton.setText("搜索新用户");
				}
			} else {
				JOptionPane.showMessageDialog(EQ.this, "请检查IP地址格式", "注意", JOptionPane.WARNING_MESSAGE);
				searchUserButton.setSelected(false);// 将按钮设为未选中状态
			}
		}
	}

	/**
	 * 用户列表的监听器
	 * 
	 * @author Administrator
	 *
	 */
	private class ChatTreeMouseListener extends MouseAdapter {
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 2) {// 当鼠标时双击时
				TreePath path = chatTree.getSelectionPath();// 获得用户树上被选中的节点路径
				if (path == null)// 如果是空的
					return;// 停止此方法
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();// 返回此路径上的组件，并转换为节点
				User user = (User) node.getUserObject();// 将节点中的用户信息取出来
				try {
					DatagramPacket packet = new DatagramPacket(new byte[0], 0, InetAddress.getByName(user.getIp()),
							1111);// 创建一个空的UDP数据包
					TelFrame.getInstance(ss, packet, chatTree);// 创建并打开聊天窗口
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 启动服务器
	 */
	private void server() {// 服务器启动方法
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (ss != null) {
						byte[] buf = new byte[4096];// 创建数据包字节数组
						DatagramPacket dp = new DatagramPacket(buf, buf.length);// 创建数据包
						try {
							ss.receive(dp);// 接收数据包
							TelFrame.getInstance(ss, dp, chatTree);// 开启聊天窗口
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}
			}
		}).start();// 启动线程
	}

	/**
	 * 右键弹出菜单添加用户
	 * 
	 * @param component
	 *            - 触发右键的组件
	 * @param popup
	 *            - 弹出式菜单
	 */
	private void addUserPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {// 鼠标抬起时
				if (e.isPopupTrigger())// 如果触发的是弹出式菜单
					showMenu(e);// 展示右键菜单栏
			}

			/**
			 * 展示右键菜单栏
			 */
			private void showMenu(MouseEvent e) {
				if (chatTree.getSelectionPaths() == null) {// 如果没有选中任何用户
					popupMenu.getComponent(0).setEnabled(false);// 关闭更名功能
					popupMenu.getComponent(2).setEnabled(false);// 关闭删除功能
					popupMenu.getComponent(3).setEnabled(false);// 关闭群发功能
					popupMenu.getComponent(4).setEnabled(false);// 关闭访问主机资源功能
				} else {
					if (chatTree.getSelectionPaths().length < 2) {// 如果选中的用户在两个以上
						popupMenu.getComponent(3).setEnabled(false);
					} else {
						popupMenu.getComponent(3).setEnabled(true);// 开启群发功能
					}
					popupMenu.getComponent(0).setEnabled(true);// 开启更名功能
					popupMenu.getComponent(2).setEnabled(true);// 开启删除功能
					popupMenu.getComponent(4).setEnabled(true);// 开启访问主机资源功能
				}
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	/**
	 * 保存主窗体位置的方法
	 */
	private void saveLocation() {
		location = getBounds();
		dao.updateLocation(location);// 保存当前窗体位置
	}

	/**
	 * 创建右键弹出菜单
	 * 
	 * @return
	 */
	protected JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.setOpaque(false);
		}
		final JMenuItem rename = new JMenuItem();
		popupMenu.add(rename);
		rename.addActionListener(new RenameActionListener());
		rename.setText("更名");
		final JMenuItem addUser = new JMenuItem();
		addUser.addActionListener(new AddUserActionListener());
		popupMenu.add(addUser);
		addUser.setText("添加用户");
		final JMenuItem delUser = new JMenuItem();
		delUser.addActionListener(new delUserActionListener());
		popupMenu.add(delUser);
		delUser.setText("删除用户");
		final JMenuItem messagerGroupSend = new JMenuItem();
		messagerGroupSend.addActionListener(new messagerGroupSendActionListener());
		messagerGroupSend.setText("信使群发");
		popupMenu.add(messagerGroupSend);
		final JMenuItem accessComputerFolder = new JMenuItem("访问主机资源");
		accessComputerFolder.setActionCommand("computer");
		popupMenu.add(accessComputerFolder);
		accessComputerFolder.addActionListener(new accessFolderActionListener());
		return popupMenu;
	}

	/**
	 * 设置状态栏信息
	 * 
	 * @param str
	 */
	public void setStatic(String str) {
		if (stateLabel != null)
			stateLabel.setText(str);
	}

	/**
	 * 显示更名对话框
	 * 
	 * @param str
	 *            - 旧名字
	 * @return 新名字
	 */
	private String showInputDialog(String str) { // 显示输入对话框
		String newName = JOptionPane.showInputDialog(this, "<html>输入<font color=red>" + str + "</font>的新名字</html>");// 打开输入对话框，利用超文本语言控制字体颜色
		return newName;
	}

	/**
	 * 访问对方共享资源文件夹
	 *
	 */
	private class accessFolderActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			TreePath path = chatTree.getSelectionPath();// 获取选中的节点路径
			if (path == null)// 如果节点路径不为空
				return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();// 获取节点路径上的节点
			User user = (User) node.getUserObject();// 获取节点中保存的用户
			String ip = "\\\\" + user.getIp();// 拼写共享文件夹地址
			String command = e.getActionCommand();// 获取组件动作指令
			if (command.equals("computer")) {// 如果是访问对方资源
				Resource.startFolder(ip);// 打开主机共享文件
			}
		}
	}

	/**
	 * 更改用户名事件
	 *
	 */
	private class RenameActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			TreePath path = chatTree.getSelectionPath();// 获取选中的节点路径
			if (path == null)// 如果节点路径不为空
				return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();// 获取节点路径上的节点
			User user = (User) node.getUserObject();// 获取节点中保存的用户
			String newName = showInputDialog(user.getName());// 更改用户名，并获得新用户名
			if (newName != null && !newName.isEmpty()) {// 如果新名字不为空
				user.setName(newName);// 更改用户信息
				dao.updateUser(user);// 更新数据库中用户资料
				DefaultTreeModel model = (DefaultTreeModel) chatTree.getModel();// 获取用户树模型
				model.reload();// 重新加载用户树
				chatTree.setSelectionPath(path);// 恢复选中状态
				initUserInfoButton();// 初始化用户信息按钮
			}
		}
	}

	/**
	 * 窗体关闭事件
	 * 
	 */
	private class FrameWindowListener extends WindowAdapter {
		public void windowClosing(final WindowEvent e) {// 窗体关闭时
			setVisible(false);// 隐藏窗体
		}
	}

	/**
	 * 添加用户动作事件
	 *
	 */
	private class AddUserActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			String ip = JOptionPane.showInputDialog(EQ.this, "输入新用户IP地址");
			if (ip != null)
				chatTree.addUser(ip, "add");
		}
	}

	/**
	 * 删除用户动作事件
	 *
	 */
	private class delUserActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			chatTree.delUser();
		}
	}

	/**
	 * 群发消息动作事件
	 *
	 */
	private class messagerGroupSendActionListener implements ActionListener {// 信使群发
		public void actionPerformed(final ActionEvent e) {
			String systemName = System.getProperty("os.name");
			if (systemName != null && systemName.equals("Windows 7")) {
				String message = JOptionPane.showInputDialog(EQ.this, "请输入群发信息", "信使群发",
						JOptionPane.INFORMATION_MESSAGE);
				if (message != null && !message.equals("")) {
					TreePath[] selectionPaths = chatTree.getSelectionPaths();// 获取用户树上同时选中的用户节点
					Resource.sendGroupMessenger(ss, selectionPaths, message);
				} else if (message != null && message.isEmpty()) {
					JOptionPane.showMessageDialog(EQ.this, "不能发送空信息！");
				}
			} else {
				JOptionPane.showMessageDialog(EQ.this, "此功能只能在Windows 7环境使用！", "提示", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * 系统栏初始化
	 */
	private void SystemTrayInitial() {
		if (!SystemTray.isSupported()) // 判断当前系统是否支持系统栏
			return;
		try {
			String title = "Chat局域网版";// 系统栏通知标题
			String company = "ZHZX OnlineJudge";// 系统通知栏内容
			SystemTray sysTray = SystemTray.getSystemTray();// 获取系统默认托盘
			Image image = Toolkit.getDefaultToolkit().getImage(EQ.class.getResource("/icons/sysTray.png"));// 创建系统栏图标
			trayicon = new TrayIcon(image, title + "\n" + company, createMenu());
			trayicon.setImageAutoSize(true);
			trayicon.addActionListener(new SysTrayActionListener());
			sysTray.add(trayicon);
			trayicon.displayMessage(title, company, MessageType.INFO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建最小化菜单栏
	 * 
	 * @return
	 */
	private PopupMenu createMenu() {
		PopupMenu menu = new PopupMenu();
		MenuItem exitItem = new MenuItem("退出");
		exitItem.addActionListener(new ActionListener() { // 系统栏退出事件
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		MenuItem openItem = new MenuItem("打开");
		openItem.addActionListener(new ActionListener() {// 系统栏打开菜单项事件
			public void actionPerformed(ActionEvent e) {
				if (!isVisible()) {
					setVisible(true);
					toFront();
				} else
					toFront();
			}
		});
		menu.add(openItem);
		menu.addSeparator();
		menu.add(exitItem);
		return menu;
	}

	/**
	 * 最小化系统栏双击事件
	 *
	 */
	class SysTrayActionListener implements ActionListener {// 系统栏双击事件
		public void actionPerformed(ActionEvent e) {
			setVisible(true);
			toFront();
		}
	}
}