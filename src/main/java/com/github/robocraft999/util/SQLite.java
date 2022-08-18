package com.github.robocraft999.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLite {

	private static Connection conn;
	private static Statement stmt;
	private static final String DB_PATH = "src/main/resources/data.db";
	public Logger dblog = LoggerFactory.getLogger("DB-Log");

	public void connect() {
		conn = null;

		try {
			File file = new File(DB_PATH);
			file.createNewFile();

			String url = "jdbc:sqlite:" + file.getPath();
			conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
			dblog.info("Datenbankverbindung hergestellt");
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			if (conn != null) {
				conn.close();
				dblog.info("Datenbankverbindung getrennt");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void onUpdate(String sql) {
		try {
			stmt.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet onQuery(String sql) {
		try {
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Logger getdblog() {
		return dblog;
	}
}
