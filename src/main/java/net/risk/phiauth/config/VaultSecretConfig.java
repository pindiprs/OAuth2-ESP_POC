package net.risk.phiauth.config;

import com.relx.rba.tardis.credential.secrets.HashiCorpSecretStore;
import com.relx.rba.tardis.runtime.configuration.ConfigurationBuilder;
import com.relx.rba.tardis.runtime.configuration.IConfiguration;
import com.relx.rba.tardis.runtime.jobs.GenericQuery;
import com.relx.rba.tardis.runtime.jobs.IQuery;
import net.risk.phiauth.service.IVaultSecretsManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.stream.Collectors;

//@Service
@Configuration
@Order(2)
public class VaultSecretManagementImp implements IVaultSecretsManagement {

    Map<String, String> envCache;

    @Autowired
    public VaultSecretManagementImp(Map<String, String> envCache) {
        this.envCache = envCache;
    }

    @Override
    public void readSecrets() {
        GenericQuery genericQuery = new GenericQuery();
        IConfiguration configuration = getConfiguration(genericQuery);

        HashiCorpSecretStore secretStore = new HashiCorpSecretStore(configuration, genericQuery);
        //List of secrets
        var listOfSecrets = secretStore.getSecretKeys(envCache.get("vault.secret.path"));

        listOfSecrets.forEach(secret -> {
            System.out.println(secret);
        });

        // connect to HashiCorp Vault
    }
    private IConfiguration getConfiguration(IQuery query) {
        // from envCache get only vault properties

        Map<String, String> vaultProperties = envCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("vault."))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        IConfiguration parentConfig = ConfigurationBuilder.getEmptyConfiguration(query);
        IConfiguration childConfig = ConfigurationBuilder.getEmptyConfiguration(query);
        vaultProperties.forEach(childConfig::setFieldString);
        parentConfig.setRecordField("secretStore", childConfig);
        return parentConfig;
    }
}
