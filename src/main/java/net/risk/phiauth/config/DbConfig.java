package net.risk.phiauth.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.risk.phiauth.constant.DBConfigKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    * Creates a DataSource with the provided database connection parameters and pool properties.
    *
    * @param url Database connection URL
    * @param userName Database username
    * @param password Database password
    * @return Configured DataSource with connection pooling
    */
   public DataSource createDataSource(String url, String userName, String password) {
       HikariConfig config = new HikariConfig();
       config.setJdbcUrl(url);
       config.setUsername(userName);
       config.setPassword(password);

       // Connection pool settings
       config.setMaximumPoolSize(10);
       config.setMinimumIdle(2);
       config.setIdleTimeout(30000);
       config.setConnectionTimeout(30000);
       config.setMaxLifetime(1800000);

       return new HikariDataSource(config);
   }

    /**
     * Primary DataSource bean to satisfy Spring Boot's requirement
     * Uses credentials from environment cache
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "phi.auth.mbs")
    public DataSource mbsDataSource() {
        String url = envCache.get(DBConfigKeys.MBS_URL_KEY);
        String username = envCache.get(DBConfigKeys.MBS_USERNAME_KEY);
        String password = envCache.get(DBConfigKeys.MBS_PASSWORD_KEY);

        return createDataSource(url, username, password);
    }

    @Bean
    @ConfigurationProperties(prefix = "phi.auth.accurint")
    public DataSource accurintDataSource() {
        String url = envCache.get(DBConfigKeys.ACCURINT_URL_KEY);
        String username = envCache.get(DBConfigKeys.ACCURINT_USERNAME_KEY);
        String password = envCache.get(DBConfigKeys.ACCURINT_PASSWORD_KEY);

        return createDataSource(url, username, password);
    }
}