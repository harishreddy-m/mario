package com.mario.persistence.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mario.model.Palace;
import com.mario.model.User;
import com.mario.persistence.UserDao;
import com.mario.utils.DBConnection;

public class UserDaoHsql implements UserDao {
	DBConnection db;

	public UserDaoHsql(DBConnection db) {
		this.db = db;
	}

	public List<User> getUsers() {
		String sql = "Select * from users";
		try {

			ResultSet resultSet = db.getConnection().createStatement().executeQuery(sql);
			List<User> usersfromDB = new ArrayList<>();
			while (resultSet.next()) {

				Palace chartData = null;
				try {
					ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(resultSet.getBytes("map")));
					chartData = (Palace) in.readObject();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				usersfromDB.add(new User(resultSet.getString("name"), resultSet.getInt("gems"), resultSet.getInt("health"), chartData));
			}
			return usersfromDB;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void persistUser(User toSave) {
		String sql = "insert into users(name,gems,health,map) values(?,?,?,?)";
		try {
			PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
			pstmt.setString(1, toSave.getName());
			pstmt.setInt(2, toSave.getGems());
			pstmt.setInt(3, toSave.getHealth());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(toSave.getMap());
			pstmt.setBinaryStream(4, new ByteArrayInputStream(baos.toByteArray()));
			pstmt.executeUpdate();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void updateUser(User player) {
		String sql = "update users set gems=?,health=?,map=? where name=?";
		try {
			PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
			pstmt.setInt(1, player.getGems());
			pstmt.setInt(2, player.getHealth());
			pstmt.setString(4, player.getName());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(player.getMap());
			pstmt.setBinaryStream(3, new ByteArrayInputStream(baos.toByteArray()));
			pstmt.executeUpdate();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

	}

}
