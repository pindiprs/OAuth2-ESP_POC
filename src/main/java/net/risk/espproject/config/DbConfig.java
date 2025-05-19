package net.risk.espproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbConfig {

    @Value("${phi.auth.datasource.url}") String dataSourceUrl;
    @Value("${phi.auth.datasource.username}") String dataSourceUsername;
    @Value("${phi.auth.datasource.password}") String dataSourcePassword;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(dataSourceUrl)
                .username(dataSourceUsername)
                .password(dataSourcePassword)
                .build();
    }
}
