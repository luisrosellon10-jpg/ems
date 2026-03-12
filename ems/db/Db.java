package ems.db;

import ems.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private Db() {}

    public static Connection getConnection() throws SQLException {
        // Ensure config is loaded
        AppConfig.loadOnce();

        String url = AppConfig.get("db.url");
        String user = AppConfig.get("db.user");
        String pass = AppConfig.get("db.password");

        return DriverManager.getConnection(url, user, pass);
    }
}