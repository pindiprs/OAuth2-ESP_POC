package net.risk.espproject.command;

import com.nimbusds.jose.shaded.gson.JsonObject;

public interface ITokenManagement {
    JsonObject getToken(String clientAuthenticationJWT, String grantType);
    JsonObject getToken(String signedTokenWithTokenId);
}

