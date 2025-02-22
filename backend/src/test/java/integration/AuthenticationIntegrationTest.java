package integration;

import com.noisevisionsoftware.nutrilog.NutrilogApplication;
import com.noisevisionsoftware.nutrilog.security.model.FirebaseUser;
import com.noisevisionsoftware.nutrilog.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = NutrilogApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void login_WithValidToken_ShouldReturnFirebaseUser() throws Exception {
        // Arrange
        FirebaseUser adminUser = FirebaseUser.builder()
                .uid("test-uid")
                .email("admin@test.com")
                .role("ADMIN")
                .build();

        when(authService.authenticateAdmin("valid-token")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("test-uid"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_WithInvalidAuthHeaderFormat_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "InvalidFormat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid authorization header format"));
    }

    @Test
    void login_WithAuthenticationFailure_ShouldReturnUnauthorized() throws Exception {
        when(authService.authenticateAdmin(anyString()))
                .thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }
}