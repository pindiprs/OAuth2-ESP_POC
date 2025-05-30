package net.risk.phiauth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.risk.phiauth.constant.DBConfigKeys;
import net.risk.phiauth.config.DbConfig;
import net.risk.phiauth.util.RealmRequestWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static net.risk.phiauth.constant.SQLQueriesConstants.ALL_DATA_FROM_REALM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CustomRealmFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

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

    private Map<String, String> envCache;

    private CustomRealmFilter filter;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        // Setup environment cache
        envCache = new HashMap<>();
        envCache.put(DBConfigKeys.MBS_URL_KEY, "jdbc:mock:url");
        envCache.put(DBConfigKeys.MBS_USERNAME_KEY, "testuser");
        envCache.put(DBConfigKeys.MBS_PASSWORD_KEY, "testpass");

        // Default mock setup
        when(dbConfig.createDataSource(anyString(), anyString(), anyString())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(ALL_DATA_FROM_REALM)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Mock resultSet to return "test-realm"
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("use_case")).thenReturn("test-realm");

        // Mock request.getContextPath() to return empty string
        when(request.getContextPath()).thenReturn("");
    }

    @Test
    void testDoFilterInternal_ValidRealm() throws ServletException, IOException, SQLException {
        // Setup
        filter = new CustomRealmFilter(dbConfig, envCache);
        when(request.getRequestURI()).thenReturn("/test-realm/oauth2/token");

        // Execute
        filter.doFilterInternal(request, response, filterChain);

        // Verify
        verify(filterChain).doFilter(any(RealmRequestWrapper.class), eq(response));
        // RealmContextHolder is cleared in finally block so we can't check its value after method execution
    }

    @Test
    void testDoFilterInternal_InvalidUrlFormat() throws ServletException, IOException, SQLException {
        // Setup
        filter = new CustomRealmFilter(dbConfig, envCache);
        when(request.getRequestURI()).thenReturn("/");

        // Execute
        filter.doFilterInternal(request, response, filterChain);

        // Verify
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
        verify(filterChain, never()).doFilter(any(), any());
    }


    @Test
    void testDoFilterInternal_RealmNotFound() throws ServletException, IOException, SQLException {
        // Setup
        filter = new CustomRealmFilter(dbConfig, envCache);
        when(request.getRequestURI()).thenReturn("/unknown-realm/oauth2/token");

        // Execute
        filter.doFilterInternal(request, response, filterChain);

        // Verify
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Realm not found in the database: unknown-realm");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testCheckRealm_DatabaseError() throws SQLException {
        // Setup - Configure SQL exception instead of IOException
        when(dbConfig.createDataSource(anyString(), anyString(), anyString())).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Execute
        filter = new CustomRealmFilter(dbConfig, envCache);

        // Verify - listOfRealms should be empty since exception occurred
        assertTrue(filter.listOfRealms.isEmpty());
    }
}