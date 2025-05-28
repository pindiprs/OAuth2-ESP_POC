package net.risk.phiauth.service.impl;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.shaded.gson.JsonObject;
import net.risk.phiauth.context.RealmContextHolder;
import net.risk.phiauth.util.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwkSourceService {

    private final KeyManagementImpl keyManagement;
    private final Map<String, JWKSource<SecurityContext>> jwkSourceCache = new ConcurrentHashMap<>();

    @Autowired
    public JwkSourceService(KeyManagementImpl keyManagement) {
        this.keyManagement = keyManagement;
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
        return (jwkSelector, securityContext) -> {
            String realm = RealmContextHolder.getRealm();
            JsonObject privateKeyRecord = keyManagement.getPrivateKey(realm);
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