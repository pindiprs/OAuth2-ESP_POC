package net.risk.espproject.service;

import com.nimbusds.jose.shaded.gson.JsonObject;

public interface IKeyManagement {
    void manageTokenKeys();
    JsonObject getTokenPublicKeys(String realm);
    JsonObject getPrivateKey(String realm);
    JsonObject getClientPublicKey(String kid);
    JsonObject getClientAttributes(String clientId);
    boolean addClientIdAndKey(String Client_Id, String Username, String Realm, JsonObject key);
    boolean rotateClientKey(String clientId, JsonObject key);
    String getClientId(String userName, String realm);
    boolean removeClientIdAndKey(String clientId, String realm);
    String getIdentityServerURI(String realm);
    boolean setIdentityServerURI(String realm, String URI);
}
