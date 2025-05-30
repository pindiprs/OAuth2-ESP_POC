package net.risk.phiauth.config;

import lombok.extern.slf4j.Slf4j;
import net.risk.phiauth.constant.OidcCustomClaims;
import net.risk.phiauth.context.RealmContextHolder;
import net.risk.phiauth.filter.CustomRealmFilter;
import net.risk.phiauth.service.impl.JwkSourceService;

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

import static net.risk.phiauth.constant.AuthConfigConstants.TOKEN_DURATION_IN_SEC;

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

    private final JwkSourceService jwkSourceService;
    private final CustomRealmFilter customRealmFilter;

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

    private void oidcConfiguration(OidcProviderConfiguration.Builder builder) {
        String realm = RealmContextHolder.getRealm();

        // First, create a map with only the claims we want
        Map<String, Object> customClaims = new HashMap<>();

        customClaims.put(OidcCustomClaims.AUTHORIZATION_ENDPOINT, issuerBaseUrl + "/" + realm + authorizationEndpoint);
        customClaims.put(OidcCustomClaims.ISSUER, issuerBaseUrl + "/" + realm);
        customClaims.put(OidcCustomClaims.JWKS_URI, issuerBaseUrl + "/" + realm + jwksEndpoint);
        customClaims.put(OidcCustomClaims.RESPONSE_TYPES_SUPPORTED, Collections.singletonList("token"));
        customClaims.put(OidcCustomClaims.TOKEN_ENDPOINT, issuerBaseUrl + "/" + realm + tokenEndpoint);
        customClaims.put(OidcCustomClaims.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, Collections.singletonList("client_secret_post"));
        customClaims.put(OidcCustomClaims.REVOCATION_ENDPOINT, issuerBaseUrl + "/" + realm + tokenRevocationEndpoint);
        customClaims.put(OidcCustomClaims.INTROSPECTION_ENDPOINT, issuerBaseUrl + "/" + realm + introspectionEndpoint);
        customClaims.put(OidcCustomClaims.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED, Collections.singletonList("ES256"));
        customClaims.put(OidcCustomClaims.SUBJECT_TYPES_SUPPORTED, Collections.singletonList("public"));
        customClaims.put(OidcCustomClaims.GRANT_TYPES_SUPPORTED, Collections.singletonList("client_credentials"));
        customClaims.put(OidcCustomClaims.USERINFO_ENDPOINT, issuerBaseUrl + "/" + realm + "/userinfo");
        customClaims.put(OidcCustomClaims.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED, Collections.singletonList("client_secret_post"));
        customClaims.put(OidcCustomClaims.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED, Collections.singletonList("client_secret_post"));

        // Replace all claims with our custom set
        builder.claims(claims -> {
            claims.clear(); // Remove all default claims
            claims.putAll(customClaims); // Add only our specified claims
        });
    }
}