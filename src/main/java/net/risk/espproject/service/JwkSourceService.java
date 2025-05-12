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


    /**
     * Lazily retrieves the JWK source for the current realm.
     * <p>
     * This method uses lazy loading to fetch the JWK source based on the realm
     * set in the {@link RealmContextHolder}. It ensures that the JWK source
     * is dynamically created for the specific realm context when accessed.
     * </p>
     *
     * @return a {@link JWKSource} instance for the current realm
     */
    public JWKSource<SecurityContext> getJwkSource() {
        return getJwkSourceForRealm();
    }

    private JWKSource<SecurityContext> getJwkSourceForRealm() {
        return (jwkSelector, securityContext) -> {
            String realm = RealmContextHolder.getRealm();
            JsonObject privateKeyRecord = jwksApiRepository.getPrivateKey(realm);
            String x = privateKeyRecord.get("x").getAsString();
            String y = privateKeyRecord.get("y").getAsString();
            String d = privateKeyRecord.get("d").getAsString();
            String kid = privateKeyRecord.get("kid").getAsString();

            ECKey ecKey = KeyUtils.generateECKey(kid, x, y, d);
            JWKSet jwkSet = new JWKSet(ecKey);
            return jwkSelector.select(jwkSet);
        };
    }


}