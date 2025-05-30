package net.risk.phiauth.config;

import lombok.extern.slf4j.Slf4j;
import net.risk.phiauth.context.RealmContextHolder;
import net.risk.phiauth.filter.CustomAuthenticationFilter;
import net.risk.phiauth.filter.CustomRealmFilter;
import net.risk.phiauth.service.impl.JwkSourceService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static net.risk.phiauth.constant.AuthConfigConstants.TOKEN_DURATION_IN_SEC;

@Slf4j
@Configuration
public class Oauth2Config {

    @Value("${esp-client-userName}")
    String espClientUserName;

    @Value("${esp-client-password}")
    String espClientPassword;

    @Autowired
    private OidcCustomConfig oidcServerConfig;

    private final JwkSourceService jwkSourceService;
    private final CustomRealmFilter customRealmFilter;
    private final CustomAuthenticationFilter customAuthenticationFilter;

    @Autowired
    public Oauth2Config(JwkSourceService jwkSourceService, CustomRealmFilter customRealmFilter, CustomAuthenticationFilter customAuthenticationFilter) {
        this.jwkSourceService = jwkSourceService;
        this.customRealmFilter = customRealmFilter;
        this.customAuthenticationFilter = customAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .addFilterAfter(customAuthenticationFilter, WebAsyncManagerIntegrationFilter.class)
                .addFilterAfter(customRealmFilter, CustomAuthenticationFilter.class)
                .with(authorizationServerConfigurer, (authServer) -> {
                    authServer.oidc(oidc ->
                            oidc.providerConfigurationEndpoint(providerConfigurationEndpoint ->
                                    // Use the method from the new class
                                    providerConfigurationEndpoint.providerConfigurationCustomizer(oidcServerConfig::oidcConfiguration)
                            )
                    );
                    authServer.registeredClientRepository(registeredClientRepository());
                });

        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient espClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientIdIssuedAt(Instant.now())
                .clientId(espClientUserName)
                .clientSecret("{noop}" + espClientPassword)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .tokenSettings(TokenSettings
                        .builder()
                        .accessTokenTimeToLive(Duration.ofSeconds(TOKEN_DURATION_IN_SEC))
                        .build()
                )
                .authorizationGrantTypes(authorizationGrantTypes -> authorizationGrantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS))
                .build();

        return new InMemoryRegisteredClientRepository(espClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // update this with lazy loading using lambda dsl
        return (jwkSelector, securityContext) -> jwkSourceService.getJwkSource().get(jwkSelector, securityContext);
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