package net.risk.phiauth.config;

import lombok.Getter;
import net.risk.phiauth.constant.ConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Order(1)
public class ServiceConfig {
    private final Logger log = LoggerFactory.getLogger(ServiceConfig.class);

    @Getter
    @Value("${phi.auth.mbs.datasource.url}") private String mbsUrl;
    @Getter
    @Value("${phi.auth.mbs.datasource.username}") private String mbsUsername;
    @Getter
    @Value("${phi.auth.mbs.datasource.password}") private String mbsPassword;

    // Accurint Datasource properties
    @Getter
    @Value("${phi.auth.accurint.datasource.url}") private String accurintUrl;
    @Getter
    @Value("${phi.auth.accurint.datasource.username}") private String accurintUsername;
    @Getter
    @Value("${phi.auth.accurint.datasource.password}") private String accurintPassword;

    // Vault properties
    @Value("${phi.auth.vault.type}") private String vaultType;
    @Value("${phi.auth.vault.url}") private String vaultUrl;
    @Value("${phi.auth.vault.token}") private String vaultToken;
    @Value("${phi.auth.vault.role-id}") private String vaultRoleId;
    @Value("${phi.auth.vault.path}") private String vaultPath;
    @Value("${phi.auth.vault.secret-id}") private String vaultSecretId;
    @Value("${phi.auth.vault.namespace}") private String vaultNamespace;

    /**
     * This method is used for passing the values to Tardis interface for HashiCorp Vault
     *
     * TODO: GET SECRET METHOD from VAULT
     * should pass the secret path and return the secret
     * if path if value starts with ${secret: someXYZ/secret} then do use this in VAULT
     * else only use the string value.
     * ONLY CACHE vault key value pairs.
     */
    @Bean
    public Map<String, String> hashiCorpVaultStoreConfig() {
        log.info("Initializing environment cache");
        Map<String, String> secretStoreConnectionMap = new HashMap<>();

        // Cache Vault properties
        secretStoreConnectionMap.put(ConfigKeys.TARDIS_VAULT_ADDRESS, vaultUrl);
        secretStoreConnectionMap.put(ConfigKeys.TARDIS_VAULT_TOKEN_KEY, vaultToken);
        secretStoreConnectionMap.put(ConfigKeys.TARDIS_VAULT_ROLE_ID_KEY, vaultRoleId);
        secretStoreConnectionMap.put(ConfigKeys.TARDIS_VAULT_SECRET_ID_KEY, vaultSecretId);
        secretStoreConnectionMap.put(ConfigKeys.TARDIS_VAULT_NAMESPACE_KEY, vaultNamespace);
        secretStoreConnectionMap.put(ConfigKeys.TARDIS_VAULT_SECRET_PATH, vaultPath);
        secretStoreConnectionMap.put(ConfigKeys.TARDIS_VAULT_TYPE, vaultType);

        log.info("Environment cache initialized with {} entries", secretStoreConnectionMap.size());
        return secretStoreConnectionMap;
    }
}
