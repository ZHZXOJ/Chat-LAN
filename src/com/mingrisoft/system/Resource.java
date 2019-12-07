package com.mingrisoft.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.mingrisoft.userList.ChatLog;
import com.mingrisoft.userList.ChatTree;
import com.mingrisoft.userList.User;

/**
 * 
 * @author Administrator
 *
 */
public class Resource {
	/**
	 * 搜索用户
	 * 
	 * @param tree
	 *            -用户树
	 * @param progressBar
	 *            -进度条
	 * @param list
	 *            - 新添加用户列表
	 * @param button
	 *            -点击搜索的按钮
	 * @param ipStart
	 *            -开始IP地址
	 * @param ipEnd
	 *            -结束IP地址
	 */
	public static void searchUsers(ChatTree tree, JProgressBar progressBar,
			JList list, JToggleButton button, String ipStart, String ipEnd) {
		String[] is = ipStart.split("\\.");// 根据字符"."对IP地址进行分割
		String[] ie = ipEnd.split("\\.");
		int[] ipsInt = new int[4];// 起始地址， 保存四个IP数字的数组
		int[] ipeInt = new int[4];// 结束地址， 保存四个IP数字的数组
		for (int i = 0; i < 4; i++) {// 为数组赋值
			ipsInt[i] = Integer.parseInt(is[i]);
			ipeInt[i] = Integer.parseInt(ie[i]);
		}
		progressBar.setIndeterminate(true);// 使用不确定进度条
		progressBar.setStringPainted(true);// 进度条中可以展示文字
		DefaultListModel model = new DefaultListModel();// 创建表单数据模型
		model.addElement("搜索结果：");// 第一行添加说明标题
		list.setModel(model);// 搜索用户表单添加数据模型
		try {
			// 创建四个整型值，分别储存IP地址的四个数值，也是循环的初始值
			int a = ipsInt[0], b = ipsInt[1], c = ipsInt[2], d = ipsInt[3];
			// 循环IP地址的第一位数字，循环范围小于用户设定的最大值，循环标签为one
			one: while (a <= ipeInt[0]) {
				// 循环IP地址的第二位数字，循环范围小于等于255，循环标签为two
				two: while (b <= 255) {
					// 循环IP地址的第三位数字，循环范围小于等于255，循环标签为three
					three: while (c <= 255) {
						// 循环IP地址的第四位数字，循环范围小于等于255，循环标签为four
						four: while (d <= 255) {
							if (!button.isSelected()) {// 如果搜索用户按钮是非选中状态
								progressBar.setIndeterminate(false);// 进度条恢复确定式的进度条
								return;// 结束方法
							}
							Thread.sleep(100);// 休眠100毫秒
							String ip = a + "." + b + "." + c + "." + d;// 拼接循环获得的IP地址
							progressBar.setString("正在搜索：" + ip);// 进度条显示文本
							if (tree.addUser(ip, "search"))// 如果可以添加到此IP上的用户
								model.addElement("<html><b><font color=green>添加"
										+ ip + "</font></b></html>");// 向树模型数组添加消息
							// 如果循环的IP地址已到达用户指定的最大范围
							if (a == ipeInt[0] && b == ipeInt[1]
									&& c == ipeInt[2] && d == ipeInt[3]) {
								break one;// 结束标签为one的循环，也就是结束最外层循环
							}
							d++;// 第四位数字自增
							if (d > 255) {// 如果第四位数字超过255
								d = 0;// 第四位数字归零
								break four;// 结束第四层循环
							}
						}
						c++;// 第三位数字递增
						if (c > 255) {// 如果第三位数字超过255
							c = 0;// 第三位数字归零
							break three;// 结束第三层循环
						}
					}
					b++;// 第二位数字递增
					if (b > 255) {// 如果第二位数字超过255
						b = 0;// 第二位数字归零
						break two;// 结束第二层循环
					}
				}
				a++;// 第一位数字递增
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			progressBar.setIndeterminate(false);
			progressBar.setString("搜索完毕");
			button.setText("搜索新用户");
			button.setSelected(false);
		}
	}

	/**
	 * 消息群发。此功能可以群发通知，每一个收到消息的用户都会弹出聊天会话框并展示群发的消息
	 * 
	 * @param ss
	 *            - UDP套接字
	 * @param selectionPaths
	 *            - 被选中的用户节点路径
	 * @param message
	 *            - 群发的消息
	 */
	public static void sendGroupMessenger(final DatagramSocket ss,
			final TreePath[] selectionPaths, final String message) {

		new Thread(new Runnable() {
			public void run() {
				MessageFrame messageFrame = new MessageFrame();// 创建群聊窗体
				messageFrame
						.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				try {
					for (TreePath path : selectionPaths) {// 遍历所选中的节点路径
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
								.getLastPathComponent();// 返回此路径上最后一个组件，并强制转换成节点
						User user = (User) node.getUserObject();// 将节点中保存的对象转换成用户对象
						messageFrame
								.setStateBarInfo("<html>正在给<font color=blue>"
										+ user.getName()
										+ "</font>发送消息……</html>");// 设置状态栏信息
						Thread.sleep(20);// 休眠20毫秒
						byte[] strData = message.getBytes();// 将要群发的消息转为字节数组
						InetAddress toAddress = InetAddress.getByName(user
								.getIp());// 创建目标用户IP地址对象
						DatagramPacket tdp = null;// 创建UDP数据包
						// 初始化数据包，参数：数据数组，数组长度，要发送的地址
						tdp = new DatagramPacket(strData, strData.length,
								toAddress, 1111);
						ss.send(tdp);// 发送数据包
						messageFrame.addMessage(user.getName() + " 发送完毕！", true);// 向群发消息窗口添加发送完毕内容
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");// 创建日期格式化类
						String title = user.getName() + "  ("
								+ sdf.format(new Date()) + ")";// 用户名称后面添加日期
						ChatLog.writeLog(user.getIp(), title);// 记录里聊天记录中的用户名和时间
						ChatLog.writeLog(user.getIp(), "[群发内容] " + message);// 记录里聊天记录中群发内容
					}
					messageFrame.setStateBarInfo("消息发送完毕,可以关闭窗口。");// 修改群发消息窗口状态栏
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();// 启动线程

	}

	/**
	 * 打开对方共享文件夹
	 * 
	 * @param str
	 *            对方IP
	 */
	public static void startFolder(String str) {
		try {
			Runtime.getRuntime().exec("cmd /c start " + str);// 运行windows的cmd命令，打开文件夹
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
