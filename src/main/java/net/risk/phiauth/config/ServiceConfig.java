package net.risk.phiauth.config;

import net.risk.phiauth.constant.DBConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@Order(HIGHEST_PRECEDENCE)
public class ServiceConfig {
    private final Logger log = LoggerFactory.getLogger(ServiceConfig.class);

    @Value("${phi.auth.mbs.datasource.url}") private String mbsUrl;
    @Value("${phi.auth.mbs.datasource.username}") private String mbsUsername;
    @Value("${phi.auth.mbs.datasource.password}") private String mbsPassword;

    // Accurint Datasource properties
    @Value("${phi.auth.accurint.datasource.url}") private String accurintUrl;
    @Value("${phi.auth.accurint.datasource.username}") private String accurintUsername;
    @Value("${phi.auth.accurint.datasource.password}") private String accurintPassword;

    // Vault properties
    @Value("${ESP_PHI_AUTH_VAULT_ADDRESS:}") private String vaultAddress;
    @Value("${ESP_PHI_AUTH_VAULT_ROLE_ID:}") private String vaultRoleId;
    @Value("${ESP_PHI_AUTH_VAULT_SECRET_ID:}") private String vaultSecretId;
    @Value("${ESP_PHI_AUTH_VAULT_NAMESPACE:}") private String vaultNamespace;

    @Bean
    public Map<String, String> envCache() {
        log.info("Initializing environment cache");
        Map<String, String> cache = new HashMap<>();

        // Cache MBS datasource properties
        cache.put(DBConfigKeys.MBS_URL_KEY, mbsUrl);
        cache.put(DBConfigKeys.MBS_USERNAME_KEY, mbsUsername);
        cache.put(DBConfigKeys.MBS_PASSWORD_KEY, mbsPassword);

        // Cache Accurint datasource properties
        cache.put(DBConfigKeys.ACCURINT_URL_KEY, accurintUrl);
        cache.put(DBConfigKeys.ACCURINT_USERNAME_KEY, accurintUsername);
        cache.put(DBConfigKeys.ACCURINT_PASSWORD_KEY, accurintPassword);

        // TODO: Add Vault integration here to fetch secrets
        // This would involve connecting to Vault using the credentials
        // and retrieving any additional secrets

        log.info("Environment cache initialized with {} entries", cache.size());
        return cache;
    }
}
