package net.risk.espproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbConfig {

    @Value("${oauth2.datasource.url}") String dataSourceUrl;
    @Value("${oauth2.datasource.username}") String dataSourceUsername;
    @Value("${oauth2.datasource.password}") String dataSourcePassword;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(dataSourceUrl)
                .username(dataSourceUsername)
                .password(dataSourcePassword)
                .build();
    }
}
