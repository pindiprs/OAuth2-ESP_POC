package net.risk.espproject.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RealmRequestWrapper extends HttpServletRequestWrapper {
    private final String strippedUri;
    private final String strippedServletPath;

    public RealmRequestWrapper(HttpServletRequest request) {
        super(request);
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI().substring(contextPath.length());

        // Splitting and removing the realm segment
        String[] parts = requestUri.split("/");
        StringBuilder sb = new StringBuilder();
        // Start at index 2 to skip the realm (index 1)
        for (int i = 2; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append("/").append(parts[i]);
            }
        }
        strippedUri = contextPath + sb.toString();
        strippedServletPath = sb.toString();
    }

    @Override
    public String getRequestURI() {
        return strippedUri;
    }

    @Override
    public String getServletPath() {
        return strippedServletPath;
    }
}