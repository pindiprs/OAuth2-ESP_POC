package net.risk.phiauth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.risk.phiauth.config.DbConfig;
import net.risk.phiauth.constant.DBConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.Map;

@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, String> envCache;

    @Value("${phi.authentication.client-id}")
    private String userId;

    @Value("${phi.authentication.client-secret}")
    private String credential;

    private static final String IDENTITY_VERIFICATION_SPROC = "{call sp_aac_authorize_v3()}";

    public CustomAuthenticationFilter(DbConfig dbConfig, Map<String, String> envCache) {
        this.envCache = envCache;
        // Create JdbcTemplate using the appropriate datasource
        String url = envCache.get(DBConfigKeys.ACCURINT_URL_KEY);
        String username = envCache.get(DBConfigKeys.ACCURINT_USERNAME_KEY);
        String password = envCache.get(DBConfigKeys.ACCURINT_PASSWORD_KEY);
        this.jdbcTemplate = new JdbcTemplate(dbConfig.createDataSource(url, username, password));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            logger.info("Executing identity verification stored procedure");

            if (userId == null || credential == null) {
                logger.error("Missing required identity parameters");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
                return;
            }

            // Use named parameters with SimpleJdbcCall
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName(DBConfigKeys.ACCURINT_DB_SPROC);

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("p_loginid", userId)
                    .addValue("p_password", credential)
                    .addValue("p_application", "")
                    .addValue("p_clientip", "10.145.44.113")
                    .addValue("p_checkpasswd", 1)
                    .addValue("p_checkip", 1)
                    .addValue("p_authenticateonly", 0)
                    .addValue("p_checksuspended", 1)
                    .addValue("p_checkpasswordexpiration", 1)
                    .addValue("p_get_company_tags_only", 0)
                    .addValue("p_active_company_id", null)
                    .addValue("p_get_all_tags", 1);


            Map<String, Object> result = jdbcCall.execute(params);

            // Assuming the stored procedure returns a boolean parameter named "is_authorized"
            String authenticationStatus = "p_successcode";
            boolean isAuthorized =  (Integer) result.get(authenticationStatus) == 0;

            if (!isAuthorized) {
                logger.warn("Authorization failed for user: {}", userId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization failed");
                return;
            }

            logger.info("Authorization successful for user: {}", userId);
            request.setAttribute("authorization_status", true);

            // Continue with filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Database error: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}