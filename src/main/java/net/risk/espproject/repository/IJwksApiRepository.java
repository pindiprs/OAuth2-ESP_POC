package net.risk.espproject.repository;

import com.nimbusds.jose.shaded.gson.JsonObject;

public interface IJwksApiRepository {
    JsonObject getPublicKey(String useCase);
    JsonObject getPrivateKey(String useCase);
}
