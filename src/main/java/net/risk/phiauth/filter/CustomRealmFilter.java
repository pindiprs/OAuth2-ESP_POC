package net.risk.phiauth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.risk.phiauth.config.DbConfig;
import net.risk.phiauth.context.RealmContextHolder;
import net.risk.phiauth.util.RealmRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.risk.phiauth.constant.SQLQueriesConstants.ALL_DATA_FROM_REALM;

@Component
public class CustomRealmFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DbConfig dbConfig;
    List<String> listOfRealms;

    public CustomRealmFilter(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.listOfRealms = new ArrayList<>();
        checkRealm();
    }

    /**
     * Filters the incoming HTTP request, extracts the realm from the URI, and sets it in the context.
     * If the realm is not found or invalid, an exception is thrown.
     *
     * @param request  the incoming HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to pass the request and response to the next filter
     * @throws ServletException if the realm is not found in the URI or other processing errors occur
     * @throws IOException if an I/O error occurs during request processing
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract realm from the URI with proper validation
            String uri = request.getRequestURI();
            String[] parts = uri.split("/");

            // Check if there are enough parts in the URL
            if (parts.length < 2) {
                logger.error("Invalid URL format, no realm segment found: {}", uri);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
                return;
            }

            String realm = parts[1];
            // Validate realm exists in our list
            if (listOfRealms.stream().noneMatch(realm::equals)) {
                logger.error("Realm not found: {}", realm);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Realm not found in the database: " + realm);
                return;
            }

            // Set the realm in the context
            logger.info("Setting realm: {}", realm);
            RealmContextHolder.setRealm(realm);

            // Wrap the request and continue the filter chain
            RealmRequestWrapper wrappedRequest = new RealmRequestWrapper(request);
            logger.info("Forwarding request to: {}", wrappedRequest.getRequestURI());
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            // Clear the realm context to avoid thread-local leaks
            RealmContextHolder.clear();
        }
    }

    private void checkRealm() {
        try{
            logger.info("Getting all realms from database");
            PreparedStatement preparedStatement = dbConfig.dataSource()
                    .getConnection()
                    .prepareStatement(ALL_DATA_FROM_REALM);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                listOfRealms.add(resultSet.getString("use_case"));
            }
        } catch (SQLException e) {
            logger.error("Error while getting all realms: {}", e.getMessage());
        }

    }
}