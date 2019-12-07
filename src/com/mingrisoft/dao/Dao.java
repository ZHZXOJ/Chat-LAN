package com.mingrisoft.dao;

import java.awt.Rectangle;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import com.mingrisoft.userList.User;

/**
 * 数据库接口类 此类使用微型数据库——Derby，所有数据文件会保存在项目根目录下自动生成的“db_EQ”文件夹中
 *
 */
public class Dao {
	private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";// 数据库驱动，使用derby数据库
	private static String url = "jdbc:derby:db_EQ";// 数据库URL
	private static Connection conn = null;// 数据库连接
	private static Dao dao = null;// 数据库接口对象，采用单例模式

	private Dao() {
		try {
			Class.forName(driver);// 加载数据库驱动
			if (!dbExists()) {// 如果数据库存在
				conn = DriverManager.getConnection(url + ";create=true");// 连接并生成数据库
				createTable();// 创建数据表格
			} else
				conn = DriverManager.getConnection(url);// 直接连接数据库
			addDefUser();// 创建本机用户
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "数据库连接异常，或者本软件已经运行。");
			System.exit(0);// 程序终止
		}
	}

	/**
	 * 测试数据库是否存在
	 * 
	 * @return 数据库是否存在
	 */
	private boolean dbExists() {
		boolean bExists = false;// 默认数据库不存在
		File dbFileDir = new File("db_EQ");// 数据库文件夹
		if (dbFileDir.exists()) {// 如果存在此文件夹
			bExists = true;// 数据库存在
		}
		return bExists;
	}

	/**
	 * 获取DAO对象
	 * 
	 * @return DAO实例对象
	 */
	public static Dao getDao() {
		if (dao == null)// 如果数据库对象是空的
			dao = new Dao();// 创建新的数据库对象
		return dao;
	}

	/**
	 * 获取所有用户
	 * 
	 * @return 获取所有用户的集合
	 */
	public List<User> getUsers() {
		List<User> users = new ArrayList<User>();
		try {
			String sql = "select * from tb_users";
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(sql);
			while (rs.next()) {
				User user = new User();
				user.setIp(rs.getString(1));
				user.setHost(rs.getString(2));
				user.setName(rs.getString(3));
				user.setTipText(rs.getString(4));
				user.setIcon(rs.getString(5));
				users.add(user);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}

	/**
	 * 获取指定IP的用户
	 * 
	 * @param ip
	 *            - IP地址
	 * @return 查到的用户信息
	 */
	public User getUser(String ip) {
		String sql = "select * from tb_users where ip=?";
		User user = null;
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, ip);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				user = new User();
				user.setIp(rs.getString(1));
				user.setHost(rs.getString(2));
				user.setName(rs.getString(3));
				user.setTipText(rs.getString(4));
				user.setIcon(rs.getString(5));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return user;
	}

	/**
	 * 添加用户
	 * 
	 * @param user
	 *            - 被添加的用户
	 */
	public void addUser(User user) {
		try {
			String sql = "insert into tb_users values(?,?,?,?,?)";
			PreparedStatement ps = null;
			ps = conn.prepareStatement(sql);
			ps.setString(1, user.getIp());
			ps.setString(2, user.getHost());
			ps.setString(3, user.getName());
			ps.setString(4, user.getTipText());
			ps.setString(5, user.getIcon());
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 修改用户
	 * 
	 * @param user
	 *            - 被修改的用户
	 */
	public void updateUser(User user) {
		try {
			String sql = "update tb_users set host=?,name=?,tooltip=?,icon=? where ip='"
					+ user.getIp() + "'";
			PreparedStatement ps = null;
			ps = conn.prepareStatement(sql);
			ps.setString(1, user.getHost());
			ps.setString(2, user.getName());
			ps.setString(3, user.getTipText());
			ps.setString(4, user.getIcon());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除用户
	 * 
	 * @param user
	 *            -被删除的用户
	 */
	public void delUser(User user) {
		try {
			String sql = "delete from tb_users where ip=?";
			PreparedStatement ps = null;
			ps = conn.prepareStatement(sql);
			ps.setString(1, user.getIp());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新主窗体在屏幕中显示的位置
	 * 
	 * @param location
	 *            - 屏幕位置对象
	 */
	public void updateLocation(Rectangle location) {
		String sql = "update tb_location set xLocation=?,yLocation=?,width=?,height=?";
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, location.x);
			pst.setInt(2, location.y);
			pst.setInt(3, location.width);
			pst.setInt(4, location.height);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取窗体位置
	 * 
	 * @return 屏幕位置对象
	 */
	public Rectangle getLocation() {
		Rectangle rec = new Rectangle(100, 0, 240, 500);
		String sql = "select * from tb_location";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				rec.x = rs.getInt(1);
				rec.y = rs.getInt(2);
				rec.width = rs.getInt(3);
				rec.height = rs.getInt(4);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rec;
	}

	/**
	 * 创建数据表格
	 */
	private void createTable() {
		String createUserSql = "CREATE TABLE tb_users ("
				+ "ip varchar(16) primary key," + "host varchar(30),"
				+ "name varchar(20)," + "tooltip varchar(50),"
				+ "icon varchar(50))";
		String createLocationSql = "CREATE TABLE tb_location ("
				+ "xLocation int," + "yLocation int," + "width int,"
				+ "height int)";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(createUserSql);
			stmt.execute(createLocationSql);
			addDefLocation();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建本机用户
	 */
	public void addDefUser() {
		try {
			InetAddress local = InetAddress.getLocalHost();// 创建本地主机对象
			User user = new User();// 创建本地用户
			user.setIp(local.getHostAddress());
			user.setHost(local.getHostName());
			user.setName(local.getHostName());
			user.setTipText(local.getHostAddress());
			user.setIcon("1.gif");// 设置头像
			if (getUser(user.getIp()) == null) {
				addUser(user);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加默认窗体位置
	 */
	public void addDefLocation() {
		String sql = "insert into tb_location values(?,?,?,?)";
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, 100);
			pst.setInt(2, 0);
			pst.setInt(3, 240);
			pst.setInt(4, 500);
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
