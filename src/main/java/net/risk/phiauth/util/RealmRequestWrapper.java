package net.risk.phiauth.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RealmRequestWrapper extends HttpServletRequestWrapper {
    private final String strippedUri;
    private final String strippedServletPath;

    public RealmRequestWrapper(HttpServletRequest request) {
        super(request);
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI().substring(contextPath.length());

        // Splitting and validating the realm segment
        String[] parts = requestUri.split("/");
        if (parts.length < 2 || parts[1].isEmpty()) {
            throw new IllegalArgumentException("Missing realm in the request URI");
        }

        // Removing the realm segment
        StringBuilder sb = new StringBuilder();
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