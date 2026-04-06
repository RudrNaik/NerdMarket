package NerdMarket.debugging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    @Autowired
    private Environment environment;

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        String port = environment.getProperty("server.port", "8080");
        long maxMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        int processors = Runtime.getRuntime().availableProcessors();
        String[] profiles = environment.getActiveProfiles();
        String activeProfiles = profiles.length > 0 ? Arrays.toString(profiles) : "[default]";

        log.info("========================================");
        log.info("  NerdMarket started successfully");
        log.info("  Port              : {}", port);
        log.info("  Max Memory        : {} MB", maxMemoryMb);
        log.info("  CPU Processors    : {}", processors);
        log.info("  Active Profiles   : {}", activeProfiles);
        log.info("========================================");
    }
}
