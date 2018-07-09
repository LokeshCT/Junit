package com.rsqe.database.utils;

import java.io.IOException;
import java.util.Properties;

public class DatabaseValues {

    public enum Direction { export, load }

    private Properties properties;

    public DatabaseValues(Direction direction) {
        this.properties = new Properties();
        try {
            this.properties.load(DatabaseValues.class.getClassLoader().getResourceAsStream(direction.name()+"_connection.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDriver() {
        return this.properties.getProperty("driver");
    }

    public String getUrl() {
        return this.properties.getProperty("url");
    }

    public String getUsername() {
        return this.properties.getProperty("username");
    }

    public String getPassword() {
        return this.properties.getProperty("password");
    }
    public String getPath() {
        return this.properties.getProperty("path");
    }
}
