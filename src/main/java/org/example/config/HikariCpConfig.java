package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Properties;

public class HikariCpConfig {
    public static HikariDataSource getHikariDataSource(){
        Properties properties = GetProperties.get();

        String jdbcUrl = properties.getProperty("myliquid.jdbc.url");
        String username = properties.getProperty("myliquid.jdbc.username");
        String password = properties.getProperty("myliquid.jdbc.password");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }
}
