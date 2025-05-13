package net.risk.espproject.service.impl;


import lombok.extern.slf4j.Slf4j;
import net.risk.espproject.constant.KEY_STATUS;
import net.risk.espproject.repository.impl.JwksApiRepository;
import net.risk.espproject.util.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
public class DbService {

    private final JwksApiRepository jwksApiRepository;
    @Autowired
    public DbService(JwksApiRepository jwksApiRepository) {
        this.jwksApiRepository = jwksApiRepository;
    }
    public String get(String realm) {
        updateKeys(realm);
        return "Hello from DbService";
    }

    private void updateKeys(String realm) {
        Map<String, String> resultSet = jwksApiRepository.getAllDataForRealm(realm);
        String status = resultSet.get("status");
        int statusInt = Integer.parseInt(status);

        for (String key : resultSet.keySet()) {
            if (KEY_STATUS.ACTIVE == KeyUtils.checkStatus(statusInt)) {
                // Parse the date_expires string to Instant
                String expireDateString = resultSet.get("date_expires");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.parse(expireDateString, formatter);
                Instant expireDate = localDateTime.toInstant(ZoneOffset.UTC);

                // Check if the expiry time is less than 48 hrs
                if (expireDate.isBefore(Instant.now().plus(48, ChronoUnit.HOURS))) {
                    rotateKeys(realm);
                }
            }
        }
    }
    private void rotateKeys(String realm){
        log.info("Rotate keys in realm: {}", realm);
    }
}
