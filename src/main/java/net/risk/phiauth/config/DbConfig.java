package net.risk.phiauth.config;

import net.risk.phiauth.constant.DBConfigKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DbConfig {

    private final Map<String, String> envCache;

    @Autowired
    public DbConfig(Map<String, String> envCache) {
        this.envCache = envCache;
    }

    /**
     * Creates a DataSource with the provided database connection parameters.
     *
     * @param url Database connection URL
     * @param userName Database username
     * @param password Database password
     * @return Configured DataSource
     */
    public DataSource createDataSource(String url, String userName, String password) {
        return DataSourceBuilder.create()
                .url(url)
                .username(userName)
                .password(password)
                .build();
    }

    /**
     * Primary DataSource bean to satisfy Spring Boot's requirement
     * Uses credentials from environment cache
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        String url = envCache.get(DBConfigKeys.MBS_URL_KEY);
        String username = envCache.get(DBConfigKeys.MBS_USERNAME_KEY);
        String password = envCache.get(DBConfigKeys.MBS_PASSWORD_KEY);

        return createDataSource(url, username, password);
    }
}