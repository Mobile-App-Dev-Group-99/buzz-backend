package com.buzzapp.attendance_service.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String url = System.getenv("SPRING_DATASOURCE_URL");
        if (url == null || url.isBlank()) {
            url = databaseUrl;
        }
        if (url != null && !url.startsWith("jdbc:")) {
            url = "jdbc:" + url;
        }
        config.setJdbcUrl(url);

        String user = System.getenv("SPRING_DATASOURCE_USERNAME");
        if (user == null || user.isBlank()) {
            user = System.getenv("POSTGRES_USER");
        }
        config.setUsername(user);

        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (password == null || password.isBlank()) {
            password = System.getenv("POSTGRES_PASSWORD");
        }
        config.setPassword(password);

        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}
