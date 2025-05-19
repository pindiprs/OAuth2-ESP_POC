package net.risk.espproject.config;

import lombok.extern.slf4j.Slf4j;
import net.risk.espproject.context.RealmContextHolder;
import net.risk.espproject.filter.CustomRealmFilter;
import net.risk.espproject.service.impl.JwkSourceService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.oidc.OidcProviderConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.risk.espproject.constant.AuthConfigConstants.TOKEN_DURATION_IN_SEC;

@Slf4j
@Configuration
public class Oauth2Config {

    @Value("${esp-client-userName}")
    String espClientUserName;

    @Value("${esp-client-password}")
    String espClientPassword;

    @Value("${phi.auth.oidc.issuer.base-url}")
    private String issuerBaseUrl;

    @Value("${phi.oauth.authorization-endpoint}")
    private String authorizationEndpoint;

    @Value("${phi.oauth.token-endpoint}")
    private String tokenEndpoint;

    @Value("${phi.oauth.token-revocation-endpoint}")
    private String tokenRevocationEndpoint;

    @Value("${phi.oauth.jwks-endpoint}")
    private String jwksEndpoint;

    @Value("${phi.oauth.introspection-endpoint}")
    private String introspectionEndpoint;

    private JwkSourceService jwkSourceService;
    private CustomRealmFilter customRealmFilter;

    @Autowired
    public Oauth2Config(JwkSourceService jwkSourceService, CustomRealmFilter customRealmFilter) {
        this.jwkSourceService = jwkSourceService;
        this.customRealmFilter = customRealmFilter;
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();




        http
                .addFilterAfter(customRealmFilter, WebAsyncManagerIntegrationFilter.class)
                .with(authorizationServerConfigurer, (authServer) -> {
                    authServer.oidc(oidc ->
                            oidc.providerConfigurationEndpoint(providerConfigurationEndpoint ->
                                    // Lazy load the realm and issuer configuration
                                    providerConfigurationEndpoint.providerConfigurationCustomizer(this::oidcConfiguration)
                            )
                    );
                    authServer.registeredClientRepository(registeredClientRepository());
                });

        return http.build();
    }


    private void oidcConfiguration(OidcProviderConfiguration.Builder oidcCustomizer) {
        String realm = RealmContextHolder.getRealm();

        Map<String, Object> claims = new HashMap<>();

        claims.put("authorization_endpoint", issuerBaseUrl + "/" + realm + authorizationEndpoint);
        claims.put("issuer", issuerBaseUrl + "/" + realm);
        claims.put("jwks_uri", issuerBaseUrl + "/" + realm + jwksEndpoint);
        claims.put("response_types_supported", Collections.singletonList("token"));
        claims.put("token_endpoint", issuerBaseUrl + "/" + realm + tokenEndpoint);
        claims.put("token_endpoint_auth_methods_supported", Collections.singletonList("client_secret_post"));
        claims.put("revocation_endpoint", issuerBaseUrl + "/" + realm + tokenRevocationEndpoint);
        claims.put("introspection_endpoint", issuerBaseUrl + "/" + realm + introspectionEndpoint);
        claims.put("id_token_signing_alg_values_supported", Collections.singletonList("ES256"));
        claims.put("subject_types_supported", Collections.singletonList("public"));
        claims.put("grant_types_supported", Collections.singletonList("client_credentials"));
        claims.put("userinfo_endpoint", issuerBaseUrl + "/" + realm + "/userinfo");
        claims.put("introspection_endpoint_auth_methods_supported", Collections.singletonList("client_secret_post"));
        claims.put("revocation_endpoint_auth_methods_supported", Collections.singletonList("client_secret_post"));


        // Replace the provided builder with our custom one
        OidcProviderConfiguration configuration = OidcProviderConfiguration.withClaims(claims).build();

        // Copy all properties from our configuration to the provided builder
        claims.forEach((key, value) -> oidcCustomizer.claim(key, value));
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
                        .accessTokenTimeToLive(Duration.ofSeconds(TOKEN_DURATION_IN_SEC))
                        .build()
                )
                .authorizationGrantTypes(authorizationGrantTypes -> {
                    authorizationGrantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
                })
                .build();

        return new InMemoryRegisteredClientRepository(espClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // update this with lazy loading using lambda dsl
        return (jwkSelector, securityContext) -> {
            return jwkSourceService.getJwkSource().get(jwkSelector, securityContext);
        };
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
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        String realm = RealmContextHolder.getRealm();

        return AuthorizationServerSettings.builder()
            .issuer(issuerBaseUrl + "/" + realm)
            .jwkSetEndpoint(jwksEndpoint)
            .tokenEndpoint(tokenEndpoint)
            .tokenRevocationEndpoint(tokenRevocationEndpoint)
            .tokenIntrospectionEndpoint(introspectionEndpoint)
            .authorizationEndpoint(authorizationEndpoint)
            .build();
    }
}