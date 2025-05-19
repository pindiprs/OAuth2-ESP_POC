package net.risk.espproject.config;

import net.risk.espproject.service.impl.KeyManagementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import static net.risk.espproject.constant.AuthConfigConstants.REFRESH_TIME_IN_MILLI_SEC;

@Configuration
@EnableScheduling
public class DaemonConfig {
    private final Logger logger = LoggerFactory.getLogger(DaemonConfig.class);

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
