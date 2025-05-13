package net.risk.espproject.service.impl;


import com.nimbusds.jose.shaded.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.risk.espproject.repository.impl.JwksApiRepository;
import net.risk.espproject.service.IKeyManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import net.risk.espproject.util.KeyUtils;

@Slf4j
@Service
public class KeyManagementImpl implements IKeyManagement {

    @Override
    public void manageTokenKeys() {

    }

    @Override
    public JsonObject getTokenPublicKeys(String realm) {
        return null;
    }

    @Override
    public JsonObject getPrivateKey(String realm) {
        return null;
    }

    @Override
    public JsonObject getClientPublicKey(String kid) {
        return null;
    }

    @Override
    public JsonObject getClientAttributes(String clientId) {
        return null;
    }

    @Override
    public boolean addClientIdAndKey(String Client_Id, String Username, String Realm, JsonObject key) {
        return false;
    }

    @Override
    public boolean rotateClientKey(String clientId, JsonObject key) {
        return false;
    }

    @Override
    public String getClientId(String userName, String realm) {
        return "";
    }

    @Override
    public boolean removeClientIdAndKey(String clientId, String realm) {
        return false;
    }

    @Override
    public String getIdentityServerURI(String realm) {
        return "";
    }

    @Override
    public boolean setIdentityServerURI(String realm, String URI) {
        return false;
    }

    private final JwksApiRepository jwksApiRepository;
    @Autowired
    public KeyManagementImpl(JwksApiRepository jwksApiRepository) {
        this.jwksApiRepository = jwksApiRepository;
    }

    public void updateKeys() {
        updateKeysForAllRealms();
    }

    private void updateKeysForAllRealms() {
        Map<String, Map<String, String>> resultSet = jwksApiRepository.getAllDataForRealm();
        for (Map.Entry<String, Map<String, String>> entry : resultSet.entrySet()) {
            String realm = entry.getKey();
            Map<String, String> keysRecords = entry.getValue();
            if (KeyUtils.rotateKeys(realm, keysRecords)) {
                log.info("Keys rotated successfully for realm: {}", realm);
            } else {
                log.info("Keys rotation not required for realm:: {}", realm);
            }
        }
    }
}
