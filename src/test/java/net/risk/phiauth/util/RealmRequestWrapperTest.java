package net.risk.phiauth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RealmRequestWrapperTest {

    @DisplayName("Testing realm forwarding")
    @Test
    void testGetRequestURI() {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getContextPath()).thenReturn("/oauth2/token");
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/realm/oauth2/token");

        RealmRequestWrapper wrapper = new RealmRequestWrapper(mockRequest);

        assertEquals("/oauth2/token", wrapper.getRequestURI());
    }
    @DisplayName("Exception thrown when realm is missing")
    @Test
    void testMissingRealmThrowsException() {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getContextPath()).thenReturn("/oauth2/token");
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/oauth2/token");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> new RealmRequestWrapper(mockRequest));

        assertEquals("Missing realm in the request URI", exception.getMessage());
    }

}