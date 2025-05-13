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
            RealmRequestWrapper wrappedRequest = new RealmRequestWrapper(request);
            String[] parts = request.getRequestURI().split("/");
            String realm = parts[1];
            if (realm.equals("oauth2")) {
                logger.error("Realm not found in URL: {}", request.getRequestURI());
                throw new ServletException("Realm not found in URL: " + request.getRequestURI());
            }

            logger.info("Setting realm: {}", realm);
            RealmContextHolder.setRealm(realm);
            request.getRequestDispatcher(wrappedRequest.getRequestURI()).forward(request, response);

            logger.info("Modified URI: {}", wrappedRequest.getRequestURI());
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            // Clear the realm context to avoid thread-local leaks
            RealmContextHolder.clear();
        }
    }
}