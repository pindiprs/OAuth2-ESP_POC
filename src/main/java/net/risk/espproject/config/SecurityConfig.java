package net.risk.espproject.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class SecurityConfig {


    private final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .requestMatchers("/oauth2/**").permitAll()
                            .requestMatchers("/api/v1/**").authenticated();
                })
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(withDefaults()));


        return http.build();
    }

}
