package com.pragma.powerup.infrastructure.out.feign;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

class FeignAuthorizationInterceptorTest {

    private final FeignAuthorizationInterceptor interceptor = new FeignAuthorizationInterceptor();
    private final RequestTemplate template = new RequestTemplate();

    @AfterEach
    void resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void applyShouldForwardTheAuthorizationHeaderOfTheCurrentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        interceptor.apply(template);

        assertTrue(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
        assertTrue(template.headers().get(HttpHeaders.AUTHORIZATION).contains("Bearer jwt-token"));
    }

    @Test
    void applyShouldNotAddTheHeaderWhenTheCurrentRequestHasNone() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        interceptor.apply(template);

        assertFalse(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void applyShouldDoNothingWhenThereIsNoCurrentRequest() {
        RequestContextHolder.resetRequestAttributes();

        assertDoesNotThrow(() -> interceptor.apply(template));

        assertFalse(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }
}
