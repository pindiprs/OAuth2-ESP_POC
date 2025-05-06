package net.risk.espproject.repository.impl;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import net.risk.espproject.config.DbConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwksApiRepositoryTest {

    @Mock
    private DbConfig dbConfig;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData resultSetMetaData;

    @InjectMocks
    private JwksApiRepository jwksApiRepository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dbConfig.dataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void testGetPublicKey() throws Exception {
        // Arrange
        String useCase = "test-use-case";
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("kid")).thenReturn("test-kid");
        when(resultSet.getString("public_key")).thenReturn("{\"key\":\"value\"}");

        // Act
        JsonObject result = jwksApiRepository.getPublicKey(useCase);

        // Assert
        JsonObject expected = JsonParser.parseString("{\"key\":\"value\"}").getAsJsonObject();
        assertEquals(expected, result);
    }

    @Test
    void testGetPrivateKey() throws Exception {
        // Arrange
        String useCase = "test-use-case";
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("kid")).thenReturn("test-kid");
        when(resultSet.getString("private_key")).thenReturn("{\"key\":\"value\"}");

        // Act
        JsonObject result = jwksApiRepository.getPrivateKey(useCase);

        // Assert
        JsonObject expected = JsonParser.parseString("{\"key\":\"value\"}").getAsJsonObject();
        assertEquals(expected, result);
    }
}