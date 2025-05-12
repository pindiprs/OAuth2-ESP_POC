package net.risk.espproject.service.impl;


import lombok.extern.slf4j.Slf4j;
import net.risk.espproject.repository.impl.JwksApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import net.risk.espproject.util.KeyUtils;

@Slf4j
@Service
public class KeyRotationService {

    private final JwksApiRepository jwksApiRepository;
    @Autowired
    public KeyRotationService(JwksApiRepository jwksApiRepository) {
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
