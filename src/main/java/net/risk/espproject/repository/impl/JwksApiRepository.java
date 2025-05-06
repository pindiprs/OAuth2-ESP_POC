package net.risk.espproject.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    @Autowired
    DbConfig dbConfig;


    /**
     * This method is used to fetch the public key from the table
     *
     * @return SQL rows in String format
     * TODO:
     *  1. This method returns normal json like JS object,
     *  2. UseCase should be either scope, or relam
     */
    @Override
    public JsonObject getPublicKey(String useCase) {
        Map<String, String> records = new HashMap<>();

        try {
            PreparedStatement result = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement("SELECT * FROM esp_oauth_test.oauth2_keys WHERE use_case = ?");
            result.setString(1, useCase);
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
     * This method is used to fetch the private key from the table
     *
     * @return SQL rows in String format
     * TODO: This method returns normal json like JS object
     */
    @Override
    public JsonObject getPrivateKey(String useCase) {
        Map<String, String> records = new HashMap<>();

        try {
            PreparedStatement preparedStatement = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement("SELECT * FROM esp_oauth_test.oauth2_keys WHERE use_case = ?");
            preparedStatement.setString(1, useCase);

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
}
