package net.risk.phiauth.service.impl;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.risk.phiauth.config.DbConfig;
import net.risk.phiauth.constant.SQLQueriesConstants;
import net.risk.phiauth.service.IKeyManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.risk.phiauth.util.KeyUtils;

import static net.risk.phiauth.constant.SQLQueriesConstants.GET_TOKEN_KEYS_USING_REALM;

@Slf4j
@Service
public class KeyManagementImpl implements IKeyManagement {

    private final DbConfig dbConfig;
    @Autowired
    public KeyManagementImpl(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public void manageTokenKeys() { updateKeysForAllRealms();}


    @Override
    public JsonObject getTokenPublicKeys(String realm) {
        Map<String, String> records = new HashMap<>();
        try {
            PreparedStatement result = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement(GET_TOKEN_KEYS_USING_REALM);
            result.setString(1, realm);
            ResultSet resultSet = result.executeQuery();

            while (resultSet.next()) {
                records.put("kid", resultSet.getString("kid"));
                records.put("public_key", resultSet.getString("public_key"));
            }
        } catch (SQLException e) {
            log.error("Error while fetching data for realm {}. Error code: {}", realm, e.getErrorCode(), e);
        }
        var publicKey = records.get("public_key");
        var publicKeyJson = JsonParser.parseString(publicKey.toString()).getAsJsonObject();
        return publicKeyJson;
    }

    @Override
    public JsonObject getPrivateKey(String realm) {
        Map<String, String> records = new HashMap<>();

        try {
            PreparedStatement preparedStatement = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement(GET_TOKEN_KEYS_USING_REALM);
            preparedStatement.setString(1, realm);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                records.put("kid", resultSet.getString("kid"));
                records.put("private_key", resultSet.getString("private_key"));
                records.put("date_expire", resultSet.getString("date_expire"));
            }
        } catch (SQLException e) {
            log.error("Error while fetching data from database", e.getErrorCode());
        }

        var privateKey = records.get("private_key");
        var privateKeyJson = JsonParser.parseString(privateKey.toString()).getAsJsonObject();
        return privateKeyJson;

    }

    @Override
    public JsonObject getClientPublicKey(String kid) {
        return null;
    }

    @Override
    public JsonObject getClientAttributes(String clientId) {
        return null;
    }

    @Override
    public boolean addClientIdAndKey(String Client_Id, String Username, String Realm, JsonObject key) {
        return false;
    }

    @Override
    public boolean rotateClientKey(String clientId, JsonObject key) {
        return false;
    }

    @Override
    public String getClientId(String userName, String realm) {
        return "";
    }

    @Override
    public boolean removeClientIdAndKey(String clientId, String realm) {
        return false;
    }

    @Override
    public String getIdentityServerURI(String realm) {
        return "";
    }

    @Override
    public boolean setIdentityServerURI(String realm, String URI) {
        return false;
    }

    private void updateKeysForAllRealms() {
        Map<String, Map<String, String>> realmMap = getAllDataForRealm();
        for (Map.Entry<String, Map<String, String>> entry : realmMap.entrySet()) {
            String realm = entry.getKey();
            Map<String, String> keysRecords = entry.getValue();
            if (KeyUtils.rotateKeys(realm, keysRecords)) {
                log.info("Keys rotated successfully for realm: {}", realm);
            } else {
                log.info("Keys rotation not required for realm: {}", realm);
            }
        }
    }

    public Map<String, Map<String, String>> getAllDataForRealm() {
        Map<String, Map<String, String>> records = new HashMap<>();

        try {

            PreparedStatement preparedStatement = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement(SQLQueriesConstants.ALL_DATA_FROM_REALM);
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
