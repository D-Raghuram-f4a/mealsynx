package com.mealsynx.db;

public final class DatabaseConfig {
    public static final String MYSQL_CONNECTION_NAME = "mealsynxf4a";
    public static final String DB_HOST = "localhost";
    public static final int DB_PORT = 3306;
    public static final String DB_SCHEMA = "mealsynx";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "password";

    public static final String JDBC_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_SCHEMA
            + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    private DatabaseConfig() {}
}
