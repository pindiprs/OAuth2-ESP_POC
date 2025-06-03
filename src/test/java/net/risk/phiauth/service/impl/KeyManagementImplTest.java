package net.risk.phiauth.service.impl;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import net.risk.phiauth.config.DbConfig;
import net.risk.phiauth.config.ServiceConfig;
import net.risk.phiauth.util.KeyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static net.risk.phiauth.constant.SQLQueriesConstants.ALL_DATA_FROM_REALM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KeyManagementImplTest {

    @Mock
    private DbConfig dbConfig;

    @Mock
    private ServiceConfig serviceConfig;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private KeyManagementImpl keyManagementImpl;
    private static final String TEST_URL = "jdbc:mock:url";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Setup ServiceConfig mock with test values
        when(serviceConfig.getMbsUrl()).thenReturn(TEST_URL);
        when(serviceConfig.getMbsUsername()).thenReturn(TEST_USERNAME);
        when(serviceConfig.getMbsPassword()).thenReturn(TEST_PASSWORD);

        // Setup database connection mocks
        when(dbConfig.createDataSource(TEST_URL, TEST_USERNAME, TEST_PASSWORD)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Create instance with mocked dependencies
        keyManagementImpl = new KeyManagementImpl(dbConfig, serviceConfig);
    }

    @Test
    void getPrivateKey_validRealm_returnsPrivateKey() throws Exception {
        // Arrange
        String realm = "test-realm";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("kid")).thenReturn("test-kid");
        when(resultSet.getString("private_key")).thenReturn("{\"key\":\"value\"}");
        when(resultSet.getString("date_expire")).thenReturn("2023-01-01 00:00:00");

        // Act
        JsonObject result = keyManagementImpl.getPrivateKey(realm);

        // Assert
        JsonObject expected = JsonParser.parseString("{\"key\":\"value\"}").getAsJsonObject();
        assertEquals(expected, result);
        verify(preparedStatement).setString(1, realm);
        verify(dbConfig).createDataSource(TEST_URL, TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    void getTokenPublicKeys_validRealm_returnsPublicKey() throws Exception {
        // Arrange
        String realm = "test-realm";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("kid")).thenReturn("test-kid");
        when(resultSet.getString("public_key")).thenReturn("{\"kty\":\"EC\",\"e\":\"AQAB\"}");

        // Act
        JsonObject result = keyManagementImpl.getTokenPublicKeys(realm);

        // Assert
        JsonObject expected = JsonParser.parseString("{\"kty\":\"EC\",\"e\":\"AQAB\"}").getAsJsonObject();
        assertEquals(expected, result);
        verify(preparedStatement).setString(1, realm);
        verify(dbConfig).createDataSource(TEST_URL, TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    void getAllDataForRealm_validData_returnsAllRecords() throws Exception {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("use_case")).thenReturn("realm1", "realm2");
        when(resultSet.getString("private_key")).thenReturn("{\"key\":\"private1\"}", "{\"key\":\"private2\"}");
        when(resultSet.getString("public_key")).thenReturn("{\"key\":\"public1\"}", "{\"key\":\"public2\"}");
        when(resultSet.getString("date_expire")).thenReturn("2023-01-01", "2023-02-01");
        when(resultSet.getString("date_added")).thenReturn("2022-01-01", "2022-02-01");
        when(resultSet.getString("status")).thenReturn("1", "1");

        // Act
        Map<String, Map<String, String>> result = keyManagementImpl.getAllDataForRealm();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey("realm1"));
        assertTrue(result.containsKey("realm2"));
        assertEquals("{\"key\":\"private1\"}", result.get("realm1").get("private_key"));
        assertEquals("{\"key\":\"public2\"}", result.get("realm2").get("public_key"));
        verify(connection).prepareStatement(ALL_DATA_FROM_REALM);
        verify(dbConfig).createDataSource(TEST_URL, TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    void getAllDataForRealm_sqlException_returnsEmptyMap() throws Exception {
        // Arrange
        when(dbConfig.createDataSource(TEST_URL, TEST_USERNAME, TEST_PASSWORD)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(ALL_DATA_FROM_REALM)).thenThrow(new SQLException("Database error"));

        // Act
        Map<String, Map<String, String>> result = keyManagementImpl.getAllDataForRealm();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dbConfig).createDataSource(TEST_URL, TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    void manageTokenKeys_callsUpdateKeysForAllRealms() throws Exception {
        // Arrange
        KeyManagementImpl spyKeyManagement = spy(keyManagementImpl);

        Map<String, Map<String, String>> mockData = new HashMap<>();
        Map<String, String> realmData = new HashMap<>();
        realmData.put("private_key", "{\"key\":\"private\"}");
        realmData.put("public_key", "{\"key\":\"public\"}");
        realmData.put("date_expire", "2023-01-01");
        mockData.put("test-realm", realmData);

        doReturn(mockData).when(spyKeyManagement).getAllDataForRealm();

        // Use try-with-resources with mockStatic to mock the static KeyUtils.rotateKeys method
        try (var keyUtilsMock = mockStatic(KeyUtils.class)) {
            keyUtilsMock.when(() -> KeyUtils.rotateKeys(anyString(), anyMap())).thenReturn(true);

            // Act
            spyKeyManagement.manageTokenKeys();

            // Assert
            keyUtilsMock.verify(() -> KeyUtils.rotateKeys("test-realm", realmData));
        }
    }
}