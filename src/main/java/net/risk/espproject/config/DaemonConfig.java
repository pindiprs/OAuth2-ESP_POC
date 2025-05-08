package net.risk.espproject.config;

import net.risk.espproject.service.impl.DbService;
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
    /**
     * TODO:
     *  1. Call the database service to get the data
     *  2. Compare as per the instructions provided by Kannan
     *  3. Update the data if needed
     *  4. Log the result
     */
    private final DbService databaseService;

    @Autowired
    public DaemonConfig(DbService databaseService) {
        this.databaseService = databaseService;
    }

    @Scheduled(fixedRate = 86400000)
    public void updateData() {
        String realm = "AccAuth";
        // use this to call value from jwkRepository
        // check logic to create new EC key
        // check logic to update the existing EC key
        // check logic to delete the existing EC key

        logger.debug("Daemon Config called:{}", databaseService.get(realm));
    }
}
