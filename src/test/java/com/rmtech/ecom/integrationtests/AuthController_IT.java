package com.rmtech.ecom.integrationtests;


import com.rmtech.ecom.Config.JwtUtil;
import com.rmtech.ecom.Config.TokenHashUtil;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Repositories.RefreshTokenRepository;
import com.rmtech.ecom.Repositories.User_Repo;
import com.rmtech.ecom.Service.RefreshTokenService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AuthController_IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private User_Repo userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TokenHashUtil tokenHashUtil;

    @Test
    void shouldLoginSuccessfully() throws Exception {

        User user = new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(
                passwordEncoder.encode("password")
        );

        userRepository.save(user);

        String requestBody = """
        {
            "username":"name",
            "password":"password"
        }
        """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accesstoken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        User user = new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        String requestBody = """
        {
            "username":"name",
            "password":"wrong-password"
        }
        """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldRejectLoginForUnknownUser() throws Exception {
        String requestBody = """
        {
            "username":"missing",
            "password":"password"
        }
        """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {

        User user = new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(
                passwordEncoder.encode("password")
        );

        userRepository.save(user);

        String refreshToken =
                refreshTokenService.createRefreshToken("name");

        String requestBody = """
        {
          "refreshToken":"%s"
        }
        """.formatted(refreshToken);

        mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accesstoken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        assertFalse(
                refreshTokenRepository
                        .findByTokenHash(tokenHashUtil.hash(refreshToken))
                        .isPresent()
        );
    }

    @Test
    void shouldRejectInvalidRefreshToken() throws Exception {
        String requestBody = """
        {
          "refreshToken":"missing-token"
        }
        """;

        mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {

        User user = new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(
                passwordEncoder.encode("password")
        );

        userRepository.save(user);

        String accessToken =
                jwtUtil.generateToken(user.getName());
        String refreshToken =
                refreshTokenService.createRefreshToken("name");

        String requestBody = """
        {
          "refreshToken":"%s"
        }
        """.formatted(refreshToken);

        mockMvc.perform(
                        post("/auth/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
        assertFalse(
                refreshTokenRepository
                        .findByTokenHash(tokenHashUtil.hash(refreshToken))
                        .isPresent()
        );
        assertTrue(redisTemplate.hasKey(accessToken));
    }

    @Test
    void shouldRejectLogoutWithoutAuthorizationHeader() throws Exception {
        String requestBody = """
        {
          "refreshToken":"token"
        }
        """;

        mockMvc.perform(
                        post("/auth/logout")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLogoutWithInvalidRefreshToken() throws Exception {
        User user = new User();
        user.setName("name");
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user.getName());
        String requestBody = """
        {
          "refreshToken":"invalid"
        }
        """;

        mockMvc.perform(
                        post("/auth/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"));
    }

}
