package net.risk.espproject.repository.impl;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import net.risk.espproject.config.DbConfig;
import net.risk.espproject.repository.IJwksApiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwksApiRepository implements IJwksApiRepository {
    Logger log = LoggerFactory.getLogger(JwksApiRepository.class);
    private final DbConfig dbConfig;

    @Autowired
    public JwksApiRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * Fetches the public key for a given use case from the database.
     * <p>
     * This method retrieves the public key associated with the specified use case
     * from the `oauth2_keys` table in the database. The result is returned as a
     * JSON object containing the key details.
     * </p>
     *
     * @param realm the use case for which the public key is to be fetched
     * @return a {@link JsonObject} containing the public key details
     */
    @Override
    public JsonObject getPublicKey(String realm) {
        Map<String, String> records = new HashMap<>();

        try {
            PreparedStatement result = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement("SELECT * FROM esp_oauth_test.oauth2_keys WHERE use_case = ?");
            result.setString(1, realm);
            ResultSet resultSet = result.executeQuery();

            while (resultSet.next()) {
                records.put("kid", resultSet.getString("kid"));
                records.put("public_key", resultSet.getString("public_key"));
            }
        } catch (SQLException e) {
            log.error("Error while fetching data from database", e.getErrorCode());
        }
        var publicKey = records.get("public_key");
        var publicKeyJson = JsonParser.parseString(publicKey.toString()).getAsJsonObject();
        return publicKeyJson;
    }

    /**
     * Fetches the private key for a given realm from the database.
     * <p>
     * This method retrieves the private key associated with the specified realm
     * from the `oauth2_keys` table in the database. The result is returned as a
     * JSON object containing the key details.
     * </p>
     *
     * @param realm the realm for which the private key is to be fetched
     * @return a {@link JsonObject} containing the private key details
     */
    @Override
    public JsonObject getPrivateKey(String realm) {
        Map<String, String> records = new HashMap<>();

        try {
            PreparedStatement preparedStatement = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement("SELECT * FROM esp_oauth_test.oauth2_keys WHERE use_case = ?");
            preparedStatement.setString(1, realm);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                records.put("kid", resultSet.getString("kid"));
                records.put("private_key", resultSet.getString("private_key"));
            }
        } catch (SQLException e) {
            log.error("Error while fetching data from database", e.getErrorCode());
        }

        var privateKey = records.get("private_key");
        var privateKeyJson = JsonParser.parseString(privateKey.toString()).getAsJsonObject();
        return privateKeyJson;

    }


    public Map<String, Map<String, String>> getAllDataForRealm() {
        Map<String, Map<String, String>> records = new HashMap<>();

        try {
            PreparedStatement preparedStatement = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement("SELECT * FROM esp_oauth_test.oauth2_keys");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, String> record = new HashMap<>();
                record.put("private_key", resultSet.getString("private_key"));
                record.put("public_key", resultSet.getString("public_key"));
                record.put("date_expire", resultSet.getString("date_expire"));
                record.put("date_added", resultSet.getString("date_added"));
                record.put("status", resultSet.getString("status"));
                records.put(resultSet.getString("use_case"), record);
            }
        } catch (SQLException e) {
            log.error("Error while fetching data from database", e.getErrorCode());
        }

        return records;
    }
}
