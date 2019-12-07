package com.mingrisoft.userList;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.mingrisoft.EQ;
import com.mingrisoft.dao.Dao;

/**
 * 用户树
 * 
 * @author Administrator
 *
 */
public class ChatTree extends JTree {
	private DefaultMutableTreeNode root;// 用户树的根节点
	private DefaultTreeModel treeModel;// 用户树数据模型
	private List<User> userMap;
	private Dao dao;// 数据库接口
	private EQ eq;// 主窗体对象

	public ChatTree(EQ eq) {
		super();
		root = new DefaultMutableTreeNode("root");// 创建根节点
		treeModel = new DefaultTreeModel(root);// 数据模型添加根节点
		userMap = new ArrayList<User>();
		dao = Dao.getDao();// 初始化数据库接口
		addMouseListener(new ThisMouseListener());// 添加自定义鼠标事件
		setRowHeight(50);// 每一样高度为50像素
		setToggleClickCount(2);// 设置节点展开或关闭之前鼠标的单击数为两次
		setRootVisible(false);// 根节点不可见
		DefaultTreeCellRenderer defaultRanderer = new DefaultTreeCellRenderer();
		// 创建用户（树）节点面板类，传入默认的三个图标
		UserTreeRanderer treeRanderer = new UserTreeRanderer(
				defaultRanderer.getOpenIcon(), defaultRanderer.getClosedIcon(),
				defaultRanderer.getLeafIcon());
		setCellRenderer(treeRanderer);// 设置树节点渲染对象，即创建用户（树）节点面板类
		setModel(treeModel);// 设置树节点数据模型
		sortUsers();// 排序用户列表
		this.eq = eq;
	}

	/**
	 * 排序用户列表
	 */
	private synchronized void sortUsers() {
		new Thread(new Runnable() {// 匿名线程内部类
					public void run() {
						try {
							Thread.sleep(100);
							root.removeAllChildren();// 根节点删除所有用户节点
							String ip = InetAddress.getLocalHost()
									.getHostAddress();// 获取本地IP
							User localUser = dao.getUser(ip);// 获取符合本地IP的用户
							if (localUser != null) {// 如果本地用户不为空
								DefaultMutableTreeNode node = new DefaultMutableTreeNode(
										localUser);// 创建本地用户节点
								root.add(node);// 把自己显示在首位
							}
							userMap = dao.getUsers();// 获取所有用户
							Iterator<User> iterator = userMap.iterator();// 获取所有用户的迭代器
							while (iterator.hasNext()) { // 迭代器依次迭代
								User user = iterator.next();// 获取用户对象
								if (user.getIp().equals(localUser.getIp())) {// 如果与本地用户IP相同
									continue;// 跳过此次循环
								}
								root.add(new DefaultMutableTreeNode(user));// 用户树根节点添加用户节点
							}
							treeModel.reload();// 用户树模型重新加载
							ChatTree.this.setSelectionRow(0);
							if (eq != null)// 如果主窗体对象不为空
								eq.setStatic("　　总人数：" + getRowCount());// 设置状态栏信息
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();// 启动线程
	}

	/**
	 * 删除用户
	 */
	public void delUser() {
		TreePath path = getSelectionPath();// 返回首选节点的路径。
		if (path == null)// 此节点不存在
			return;
		User user = (User) ((DefaultMutableTreeNode) path
				.getLastPathComponent()).getUserObject();// 获取此节点的用户对象
		int operation = JOptionPane.showConfirmDialog(this, "确定要删除用户：" + user
				+ "?", "删除用户", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);// 弹出确认对话框
		if (operation == JOptionPane.YES_OPTION) {// 选择确定
			dao.delUser(user);// 数据库删除用户资料
			root.remove((DefaultMutableTreeNode) path.getLastPathComponent());// 根节点删除对应的用户节点
			treeModel.reload();// 数据库模型重新载入数据
		}
	}

	/**
	 * 添加用户
	 * 
	 * @param ip
	 *            - 用户IP
	 * @param opration
	 *            - 调用此方法的属于那种业务
	 * @return -是否添加成功
	 */
	public boolean addUser(String ip, String opration) {
		try {
			if (ip == null)// 如果IP不是空
				return false;
			User oldUser = dao.getUser(ip);// 查找此IP是否存在过
			if (oldUser == null) {// 如果数据库中不存在该用户
				InetAddress addr = InetAddress.getByName(ip);// 创建IP地址对象
				if (addr.isReachable(1500)) {// 如果在1500毫秒内可以到达该地址
					String host = addr.getHostName();// 获取这个地址的名称
					// 创建新的用户节点
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
							new User(host, ip));
					root.add(newNode);// 新节点添加到用户树中
					User newUser = new User();// 创建新用户对象
					newUser.setIp(ip);// 记录IP
					newUser.setHost(host);// 记录用户地址名
					newUser.setName(host);// 记录用户名
					newUser.setIcon("1.gif");// 记录用户头像
					dao.addUser(newUser);// 向数据库中添加此用户信息
					sortUsers();// 用户列表重新排序
					if (!opration.equals("search"))// 如果调用的此方法不是查询业务
						JOptionPane.showMessageDialog(EQ.frame, "用户" + host
								+ "添加成功", "添加用户",
								JOptionPane.INFORMATION_MESSAGE);
					return true;

				} else {
					if (!opration.equals("search"))// 如果调用的此方法不是查询业务
						// 弹出错误对话框
						JOptionPane.showMessageDialog(EQ.frame, "检测不到用户IP："
								+ ip, "错误添加用户", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} else {
				if (!opration.equals("search"))// 如果调用的此方法不是查询业务
					// 弹出警告对话框
					JOptionPane.showMessageDialog(EQ.frame, "已经存在用户IP" + ip,
							"不能添加用户", JOptionPane.WARNING_MESSAGE);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取用户树节点数据模型
	 * 
	 * @return 用户树节点数据模型
	 */
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * 自定义鼠标监听事件
	 * 
	 */
	private class ThisMouseListener extends MouseAdapter {
		public void mousePressed(final MouseEvent e) {// 鼠标按下事件
			if (e.getButton() == 3) {
				TreePath path = getPathForLocation(e.getX(), e.getY());// 获得此坐标上的节点
				if (!isPathSelected(path))// 如果此节点没有被选中
					setSelectionPath(path);// 选中此节点
			}
		}
	}
}
