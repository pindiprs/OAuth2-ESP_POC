package net.risk.espproject.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.risk.espproject.context.RealmContextHolder;
import net.risk.espproject.util.RealmRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CustomFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

   @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Expecting URL: /{realm}/oauth2/token
            String[] parts = request.getRequestURI().split("/");
            if (parts.length > 1) {
                String realm = parts[1];
                logger.info("Setting realm: {}", realm);
                RealmContextHolder.setRealm(realm);
                request.getRequestDispatcher("/oauth2/token").forward(request, response);
                return;
            }

            // Wrap the request to modify the URI and servlet path
            RealmRequestWrapper wrappedRequest = new RealmRequestWrapper(request);

            logger.info("Modified URI: {}", wrappedRequest.getRequestURI());
            logger.info("Modified Servlet Path: {}", wrappedRequest.getServletPath());
            // Pass the wrapped request to the next filter
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            // Clear the realm context to avoid thread-local leaks
            RealmContextHolder.clear();
        }
    }
}