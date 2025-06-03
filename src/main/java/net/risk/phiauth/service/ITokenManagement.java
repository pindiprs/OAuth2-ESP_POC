package net.risk.phiauth.service;

import com.nimbusds.jose.shaded.gson.JsonObject;

public interface ITokenManagement {
    JsonObject getToken(String clientAuthenticationJWT, String grantType);
    JsonObject getToken(String signedTokenWithTokenId);
}

