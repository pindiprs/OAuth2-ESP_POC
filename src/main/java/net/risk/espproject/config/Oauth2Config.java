package net.risk.espproject.config;

import net.risk.espproject.context.RealmContextHolder;
import net.risk.espproject.filter.CustomFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import net.risk.espproject.service.JwkSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
public class Oauth2Config {

    @Value("${esp-client-userName}")
    String espClientUserName;

    @Value("${esp-client-password}")
    String espClientPassword;

    JwkSourceService jwkSourceService;
    CustomFilter customFilter;

    @Autowired
    public Oauth2Config(JwkSourceService jwkSourceService, CustomFilter customFilter) {
        this.jwkSourceService = jwkSourceService;
        this.customFilter = customFilter;
    }


    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .addFilterBefore(customFilter, SecurityContextPersistenceFilter.class)
                .with(authorizationServerConfigurer, (authServer) -> {
                    authServer
                            .oidc(Customizer.withDefaults())
                            .registeredClientRepository(registeredClientRepository());

                });

        return http.build();
    }

    /**
     * Registered client repository
     *
     * @return registered client repository for OAuth 2.0 client registration
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient espClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientIdIssuedAt(Instant.now())
                .clientId(espClientUserName)
                .clientSecret("{noop}" + espClientPassword)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .tokenSettings(TokenSettings
                        .builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(60))
                        .build()
                )
                .authorizationGrantTypes(authorizationGrantTypes -> {
                    authorizationGrantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
                })
                .scope("read")
                .build();

        return new InMemoryRegisteredClientRepository(espClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return jwkSourceService.getJwkSource();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(jwkSourceService.getJwkSource());
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            // Set the desired signature algorithm dynamically
            context.getJwsHeader().algorithm(SignatureAlgorithm.ES256);
            context.getClaims().claim("client_id", espClientUserName);
            // Retrieve the current realm from the ContextHolder set in CustomFilter
            String realm = RealmContextHolder.getRealm();
            context.getClaims().claim("realm", realm);
        };
    }
}