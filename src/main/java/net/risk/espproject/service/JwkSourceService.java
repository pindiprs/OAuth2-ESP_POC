package net.risk.espproject.service;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.shaded.gson.JsonObject;
import net.risk.espproject.context.RealmContextHolder;
import net.risk.espproject.repository.impl.JwksApiRepository;
import net.risk.espproject.util.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwkSourceService {

    private final JwksApiRepository jwksApiRepository;
    private final Map<String, JWKSource<SecurityContext>> jwkSourceCache = new ConcurrentHashMap<>();

    @Autowired
    public JwkSourceService(JwksApiRepository jwksApiRepository) {
        this.jwksApiRepository = jwksApiRepository;
    }

    public JWKSource<SecurityContext> getJwkSource() {
        String realm = RealmContextHolder.getRealm();
        return jwkSourceCache.computeIfAbsent(realm, this::createJwkSource);
    }

    /**
     * Create a JWK source from the private key record. This is fetched from the Database
     * based on the current realm context.
     * @param realm the realm to use for fetching the key
     * @return JWKSource for the specified realm
     */
    public JWKSource<SecurityContext> createJwkSource(String realm) {
        JsonObject privateKeyRecord = jwksApiRepository.getPrivateKey(realm);

        String x = privateKeyRecord.get("x").getAsString();
        String y = privateKeyRecord.get("y").getAsString();
        String d = privateKeyRecord.get("d").getAsString();
        String kid = privateKeyRecord.get("kid").getAsString();

        ECKey ecKey = KeyUtils.generateECKey(kid, x, y, d);
        JWKSet jwkSet = new JWKSet(ecKey);

        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }
}