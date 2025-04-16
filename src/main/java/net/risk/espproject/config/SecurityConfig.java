package net.risk.espproject.config;


import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.shaded.gson.JsonObject;
import net.risk.espproject.repository.impl.JwksApiRepository;

import net.risk.espproject.util.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;

import java.util.UUID;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${esp-client-userName}") String espClientUserName;
    @Value("${esp-client-password}") String espClientPassword;

    @Autowired
    private JwksApiRepository jwksApiRepository;

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authServer) -> {
                    authServer
                            .registeredClientRepository(registeredClientRepository());
                });

        return http.build();
    }

    /**
     * Registered client repository
     * @return registered client repository for OAuth 2.0 client registration
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient espClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientIdIssuedAt(Instant.now())
                .clientId(espClientUserName)
                .clientSecret("{noop}"+espClientPassword)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantTypes( authorizationGrantTypes -> {
                    authorizationGrantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
                })
                .scope("read")
                .build();

        return new InMemoryRegisteredClientRepository(espClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        JsonObject privateKeyRecord = jwksApiRepository.getPrivateKey("AccAuth");

        String x = privateKeyRecord.get("x").getAsString();
        String y = privateKeyRecord.get("y").getAsString();
        String d = privateKeyRecord.get("d").getAsString();
        String kid = privateKeyRecord.get("kid").getAsString();

        ECKey ecKey = KeyUtils.generateECKey(kid, x, y, d);

        JWKSet jwkSet = new JWKSet(ecKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            // Set the desired signature algorithm dynamically
            context.getJwsHeader().algorithm(SignatureAlgorithm.ES256);
        };
    }

}
