package com.mingrisoft.frame;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import com.mingrisoft.EQ;

/**
 * 截图工具类 此类本身是一个JWindow窗体，窗体会覆盖整个屏幕，覆盖之前会捕获当期桌面的影响图片，但所有的操作其实是在此窗体上的。
 *
 */
public class CaptureScreenUtil extends JWindow {
	private int startX, startY;// 鼠标定位的开始坐标
	private int endX, endy;// 鼠标定位的结束坐标
	private BufferedImage screenImage = null;// 桌面全屏图片
	private BufferedImage tempImage = null;// 修改后成暗灰色的全屏图片
	private BufferedImage saveImage = null;// 截图
	private ToolsWindow toolWindow = null;// 工具栏窗体
	private Toolkit tool = null;// 组件工具包

	public CaptureScreenUtil() {
		tool = Toolkit.getDefaultToolkit();// 创建系统该默认组件工具包
		Dimension d = tool.getScreenSize();// 获取屏幕尺寸，赋给一个二维坐标对象
		setBounds(0, 0, d.width, d.height);// 设置截图窗口坐标和大小

		Robot robot;// 创建Java自动化测试类
		try {
			robot = new Robot();
			Rectangle fanwei = new Rectangle(0, 0, d.width, d.height);// 创建区域范围类
			screenImage = robot.createScreenCapture(fanwei);// 捕捉此区域所有像素所生成的图像
			addAction();// 添加动作监听
			setVisible(true);// 窗体可见
		} catch (AWTException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "截图功能无法使用", "错误",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * 添加动作监听
	 */
	private void addAction() {
		// 截图窗体添加鼠标事件监听
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {// 鼠标按下时
				startX = e.getX();// 记录此时鼠标横坐标
				startY = e.getY();// 记录此时鼠标纵坐标

				if (toolWindow != null) {// 如果工具栏窗体对象已存在
					toolWindow.setVisible(false);// 让工具栏窗体隐藏
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {// 鼠标松开时
				if (toolWindow == null) {// 如果工具栏窗体对象是null
					toolWindow = new ToolsWindow(e.getX(), e.getY());// 创建新的工具栏窗体
				} else {
					toolWindow.setLocation(e.getX(), e.getY());// 指定工具栏窗体在屏幕上的位置
				}
				toolWindow.setVisible(true);// 工具栏窗显示
				toolWindow.toFront();// 工具栏窗体置顶
			}
		});
		// 截图窗体添加鼠标拖拽事件监听
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {// 当鼠标被按下并拖拽时
				// 记录鼠标拖动轨迹
				endX = e.getX();// 横坐标
				endy = e.getY();// 纵坐标

				// 临时图像，用于缓冲屏幕区域放置屏幕闪烁
				Image backgroundImage = createImage(getWidth(), getHeight());// 创建背景图像
				Graphics g = backgroundImage.getGraphics();// 获得背景图像的绘图对象
				g.drawImage(tempImage, 0, 0, null);// 在背景中绘制暗灰色的屏幕图片
				int x = Math.min(startX, endX);// 在鼠标起始位置和结束位置区一个最小的
				int y = Math.min(startY, endy);// 在鼠标起始位置和结束位置区一个最小的
				int width = Math.abs(endX - startX) + 1;// 图片最小宽度为1像素
				int height = Math.abs(endy - startY) + 1;// 图片最小高度为1像素
				g.setColor(Color.BLUE);// 使用蓝色画笔画边框
				g.drawRect(x - 1, y - 1, width + 1, height + 1);// 画一个矩形，流出一个像素的距离让边框可以显示
				saveImage = screenImage.getSubimage(x, y, width, height);// 截图全屏图片
				g.drawImage(saveImage, x, y, null);// 在背景中绘制截取出的图片
				getGraphics().drawImage(backgroundImage, 0, 0,
						CaptureScreenUtil.this);// 背景图像
			}
		});
	}

	/**
	 * 绘制组件
	 */
	public void paint(Graphics g) {
		RescaleOp ro = new RescaleOp(0.5f, 0, null);// 创建RescaleOp对象，来更改图片每个颜色的颜色偏差，颜色偏移量为0.5f(暗色)
		tempImage = ro.filter(screenImage, null);// 将屏幕图片的每个像素进行颜色调整，付给临时的图片对象
		g.drawImage(tempImage, 0, 0, this);// 重新绘制暗灰色的屏幕图片
	}

	/**
	 * 将当前截图保存到本地
	 */
	public void saveImage() {
		JFileChooser jfc = new JFileChooser();// 创建文件过滤器
		jfc.setDialogTitle("保存图片");// 设置文件选择器标题
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG",
				"jpg");// 创建文件过滤器，只显示.jpg后缀的图片
		jfc.setFileFilter(filter);// 文件选择器使用过滤器
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");// 创建日期格式化类
		String fileName = sdf.format(new Date());// 将当前日期作为文件名
		FileSystemView view = FileSystemView.getFileSystemView();// 获取系统文件视图类
		File filePath = view.getHomeDirectory();// 获取桌面路径
		File saveFile = new File(filePath, fileName + ".jpg");// 创建要被保存的图片文件
		jfc.setSelectedFile(saveFile);// 将文件选择器的默认选中文件设为saveFile
		int flag = jfc.showSaveDialog(this);// 在主窗体中弹出文件选择器，获取用户操作码
		if (flag == JFileChooser.APPROVE_OPTION) {// 如果选中的是保存按钮
			try {
				ImageIO.write(saveImage, "jpg", saveFile);// 生成jpg格式的图片文件
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "文件无法保存！", "错误",
						JOptionPane.ERROR);
			} finally {
				disposeAll();// 销毁所有截图窗体
			}
		}
	}

	/**
	 * 把截图放入剪切板
	 */
	private void imagetoClipboard() {
		// 创建传输接口的对象，使用接口本身创建内部类
		Transferable trans = new Transferable() {
			@Override
			/**
			 * 返回将要被传输的对象的数据特征数组
			 */
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.imageFlavor };
			}

			@Override
			/**
			 * 判断参数传入的特征是否符合我们要求的特征
			 */
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.imageFlavor.equals(flavor);// 传入的参数是否具有图片特征
			}

			@Override
			/**
			 * 返回将要被传输的对象
			 */
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				if (isDataFlavorSupported(flavor)) {// 如果传入的特征符合图片特征
					return saveImage;// 返回截图
				}
				return null;
			}
		};
		Clipboard clipboard = tool.getSystemClipboard();// 获得系统剪切板对象
		clipboard.setContents(trans, null);// 将当前截图放入剪切板
	}

	/**
	 * 工具栏窗体
	 */
	private class ToolsWindow extends JWindow {
		/**
		 * 工具栏窗体构造方法
		 * 
		 * @param x
		 *            - 工具栏显示的横坐标
		 * @param y
		 *            - 工具栏显示的横坐标
		 */
		public ToolsWindow(int x, int y) {
			setLocation(x, y);// 设定窗体在屏幕中显示的位置

			JPanel mainPanel = new JPanel();// 主容器面板
			mainPanel.setLayout(new BorderLayout());// 容器使用边界布局

			JToolBar toolBar = new JToolBar();// 工具栏
			toolBar.setFloatable(false); // 工具栏不可拖动
			JButton saveButton = new JButton();// 保存按钮
			Icon saveIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/CaptureScreen/save.png"));
			saveButton.setIcon(saveIcon);
			saveButton.addActionListener(new ActionListener() {// 按钮点击事件
						@Override
						public void actionPerformed(ActionEvent e) {
							saveImage();// 保存当前截图
						}
					});
			toolBar.add(saveButton);// 工具栏添加按鈕

			JButton closeButton = new JButton();// 关闭按钮
			Icon closeIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/CaptureScreen/close.png"));
			closeButton.setIcon(closeIcon);
			closeButton.addActionListener(new ActionListener() {// 按钮点击事件
						@Override
						public void actionPerformed(ActionEvent e) {
							disposeAll();// 销毁全部窗体
						}
					});
			toolBar.add(closeButton);

			JButton copyButton = new JButton();// 将图片放入剪切板按钮
			Icon copyIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/CaptureScreen/copy.png"));
			copyButton.setIcon(copyIcon);// 载入图标
			copyButton.addActionListener(new ActionListener() {// 按钮点击事件
						public void actionPerformed(ActionEvent e) {
							imagetoClipboard();// 将当前截图放入剪切板
							disposeAll();// 销毁全部窗体
						}
					});
			toolBar.add(copyButton);// 工具栏添加此按钮

			mainPanel.add(toolBar, BorderLayout.NORTH);// 将工具放在容器南部
			setContentPane(mainPanel);// 窗体加载主容器面板
			pack();// 自动调整窗体大小
			setVisible(true);// 显示窗体

		}

	}

	/**
	 * 销毁所有截图窗体
	 */
	public void disposeAll() {
		toolWindow.dispose();// 销毁工具栏窗体
		dispose();// 销毁工具类窗体
	}

	public static void main(String[] args) {
		new CaptureScreenUtil();
	}
}
