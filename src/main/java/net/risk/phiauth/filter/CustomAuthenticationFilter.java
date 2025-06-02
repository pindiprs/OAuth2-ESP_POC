package net.risk.phiauth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.risk.phiauth.constant.AcurrientAuthenticationStatus;
import net.risk.phiauth.service.impl.AuthenticationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AuthenticationServiceImpl authenticationServiceImpl;

    /**
     * Hardcoded user ID and credential for testing purposes.
     */
    @Value("${phi.authentication.client-id}")
    private String userId;

    @Value("${phi.authentication.client-secret}")
    private String credential;

    public CustomAuthenticationFilter(AuthenticationServiceImpl authenticationServiceImpl) {
        this.authenticationServiceImpl = authenticationServiceImpl;
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

            AcurrientAuthenticationStatus status = authenticationServiceImpl.authenticate(userId, credential);

            if (status != AcurrientAuthenticationStatus.SUCCESS) {
                logger.warn("Authorization failed for user: {} with status: {}", userId, status);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization failed");
                return;
            }

            logger.info("Authorization successful for user: {}", userId);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Database error: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}