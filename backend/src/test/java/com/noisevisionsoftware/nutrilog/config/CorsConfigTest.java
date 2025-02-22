package com.noisevisionsoftware.nutrilog.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CorsConfigTest {

    @Test
    void corsConfigurer_ShouldConfigureCorrectly() {
        CorsConfig config = new CorsConfig();
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration corsRegistration = mock(CorsRegistration.class);

        when(registry.addMapping("/api/**")).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins("http://localhost:5173", "http://localhost:5174")).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders("*")).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(true)).thenReturn(corsRegistration);

        WebMvcConfigurer configurer = config.corsConfigurer();
        configurer.addCorsMappings(registry);

        verify(registry).addMapping("/api/**");
        verify(corsRegistration).allowedOrigins("http://localhost:5173", "http://localhost:5174");
        verify(corsRegistration).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
        verify(corsRegistration).allowedHeaders("*");
        verify(corsRegistration).allowCredentials(true);
    }
}