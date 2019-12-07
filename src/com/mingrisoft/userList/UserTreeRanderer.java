package com.mingrisoft.userList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import com.mingrisoft.EQ;

/**
 * 用户（树）节点面板类
 *
 */
public class UserTreeRanderer extends JPanel implements TreeCellRenderer {
	private Icon openIcon;// 节点展开的时的图标
	private Icon closedIcon;// 节点关闭时的图标
	private Icon leafIcon;// 节点默认图标
	private String tipText = "";// 提示内容
	private final JCheckBox label = new JCheckBox();// 单选框，文本用户显示用户名
	private final JLabel headImg = new JLabel();// 头像
	private static User user;// 用户

	public UserTreeRanderer() {
		super();
		user = null;
	}

	/**
	 * 用户（数）节点面板类构造方法
	 * 
	 * @param open
	 *            - 节点展开的时的图标
	 * @param closed
	 *            - 节点关闭时的图标
	 * @param leaf
	 *            - 节点默认图标
	 */
	public UserTreeRanderer(Icon open, Icon closed, Icon leaf) {
		openIcon = open;// 节点展开的时的图标
		closedIcon = closed;// 节点关闭时的图标
		leafIcon = leaf;// 节点默认图标
		setBackground(new Color(0xF5B9BF));// 设置背景颜色
		label.setFont(new Font("宋体", Font.BOLD, 14));// 设置单选框字体
		URL trueUrl = EQ.class
				.getResource("/image/chexkBoxImg/CheckBoxTrue.png");// 选中图标
		label.setSelectedIcon(new ImageIcon(trueUrl));// 设置单选框的选中图标
		URL falseUrl = EQ.class
				.getResource("/image/chexkBoxImg/CheckBoxFalse.png");// 未选中图标
		label.setIcon(new ImageIcon(falseUrl));// 单选框载入未选中图片
		label.setForeground(new Color(0, 64, 128));// 单选框字体颜色
		final BorderLayout borderLayout = new BorderLayout();// 创建边界布局
		setLayout(borderLayout);// 节点面板使用边界布局
		user = null;// 清空用户资料
	}

	/**
	 * @param tree
	 * @param value
	 *            - 当前树单元格的值
	 * @param selected
	 *            - 单元格已选择
	 * @param expanded
	 *            - 是否当前扩展该节点
	 * @param leaf
	 *            - 该节点是否为叶节点
	 * @param row
	 *            - 放置位置
	 * @param hasFocus
	 *            - 该节点是否拥有焦点
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof DefaultMutableTreeNode) {// 如果单元格的值属于节点值
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;// 转为节点对象
			Object uo = node.getUserObject();// 获取节点中的用户对象
			if (uo instanceof User)// 如果属于本项目中的用户类
				user = (User) uo;// 转为用户对象
		} else if (value instanceof User)// 如果单元格的值属于本项目中的用户类
			user = (User) value;// 转为用户对象
		if (user != null && user.getIcon() != null) {// 如果用户为空或者用户没有头像
			int width = EQ.frame.getWidth();// 获得窗体宽度
			if (width > 0)// 如果宽度大于0
				setPreferredSize(new Dimension(width, user.getIconImg()
						.getIconHeight()));// 设置用户节点面板宽度
			headImg.setIcon(user.getIconImg());// 设置头像图片
			tipText = user.getName();// 设置提示内容
		} else {
			if (expanded)// 扩展该节点时
				headImg.setIcon(openIcon);// 使用节点展开的时的图标
			else if (leaf)// 如果是叶子节点时
				headImg.setIcon(leafIcon);// 节点默认图标
			else
				// 否则
				headImg.setIcon(closedIcon);// 节点关闭时的图标

		}
		add(headImg, BorderLayout.WEST);// 头像放到面板西部
		label.setText(value.toString());// 设置单选框的值
		label.setOpaque(false);// 不绘制边界
		add(label, BorderLayout.CENTER);
		if (selected) {// 如果节点被选中
			label.setSelected(true);// 单选框被选中
			setBorder(new LineBorder(new Color(0xD46D73), 2, false));// 设置线布局
			setOpaque(true);// 绘制边界
		} else {
			setOpaque(false);// 不绘制边界
			label.setSelected(false);// 单选框未选中
			setBorder(new LineBorder(new Color(0xD46D73), 0, false));// 设置线布局
		}
		return this;
	}

	/**
	 * 获取提示内容
	 */
	public String getToolTipText() {
		return tipText;
	}
}
