package net.risk.espproject.service;

import com.nimbusds.jose.jwk.JWK;

import java.util.Set;

public interface IClientManagement {
    Set<JWK> getPublicKeys(String realm);
    String getPrivateKey(String realm);
    void managePublicKeys(String realm, Set<JWK> jwks);
}
