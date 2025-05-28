package net.risk.phiauth.config;

import net.risk.phiauth.service.impl.KeyManagementImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import static net.risk.phiauth.constant.AuthConfigConstants.REFRESH_TIME_IN_MILLI_SEC;

@Configuration
@EnableScheduling
public class DaemonConfig {

    private final KeyManagementImpl keyRotationService;

    @Autowired
    public DaemonConfig(KeyManagementImpl keyRotationService) {
        this.keyRotationService = keyRotationService;
    }

    @Scheduled(fixedRate = REFRESH_TIME_IN_MILLI_SEC)
    public void updateData() {
        keyRotationService.manageTokenKeys();
    }
}
