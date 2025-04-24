package net.risk.espproject.service;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.shaded.gson.JsonObject;
import net.risk.espproject.repository.impl.JwksApiRepository;
import net.risk.espproject.util.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwkSourceService {

    private final JwksApiRepository jwksApiRepository;
    private JWKSource<SecurityContext> cachedJwkSource;

    @Autowired
    public JwkSourceService(JwksApiRepository jwksApiRepository) {
        this.jwksApiRepository = jwksApiRepository;
    }

    public JWKSource<SecurityContext> getJwkSource() {
        if (cachedJwkSource == null) {
            cachedJwkSource = createJwkSource();
        }
        return cachedJwkSource;
    }

    public JWKSource<SecurityContext> createJwkSource() {
        String accAuth = "AccAuth";
        JsonObject privateKeyRecord = jwksApiRepository.getPrivateKey(accAuth);

        String x = privateKeyRecord.get("x").getAsString();
        String y = privateKeyRecord.get("y").getAsString();
        String d = privateKeyRecord.get("d").getAsString();
        String kid = privateKeyRecord.get("kid").getAsString();

        ECKey ecKey = KeyUtils.generateECKey(kid, x, y, d);
        JWKSet jwkSet = new JWKSet(ecKey);

        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }
}