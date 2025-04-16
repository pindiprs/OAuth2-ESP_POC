package net.risk.espproject.repository;

import com.nimbusds.jose.shaded.gson.JsonObject;

public interface IJwksApiRepository {
    String getAllMetaData();
    JsonObject getPublicKey(String useCase);
    JsonObject getPrivateKey(String useCase);
    void save(String data);
}
