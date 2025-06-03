package net.risk.phiauth.config;

import net.risk.phiauth.constant.OidcCustomClaims;
import net.risk.phiauth.context.RealmContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.oidc.OidcProviderConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OidcCustomConfig {

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

    public void oidcConfiguration(OidcProviderConfiguration.Builder builder) {
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
