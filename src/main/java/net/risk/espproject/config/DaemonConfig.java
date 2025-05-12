package net.risk.espproject.config;

import net.risk.espproject.service.impl.KeyRotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class DaemonConfig {
     private final Logger logger = LoggerFactory.getLogger(DaemonConfig.class);

    private final KeyRotationService keyRotationService;

    @Autowired
    public DaemonConfig(KeyRotationService keyRotationService) {
        this.keyRotationService = keyRotationService;
    }

    @Scheduled(fixedRate = 86400000)
    public void updateData() {
        keyRotationService.updateKeys();
    }
}
