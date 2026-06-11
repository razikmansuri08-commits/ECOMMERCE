package com.rmtech.ecom.unittests;

import com.rmtech.ecom.Config.TokenHashUtil;
import com.rmtech.ecom.Entities.RefreshToken;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.RefreshTokenRepository;
import com.rmtech.ecom.Repositories.User_Repo;
import com.rmtech.ecom.Service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private User_Repo userRepo;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenHashUtil tokenHashUtil;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldCreateRefreshTokenWithHashAndExpiry() {
        User user = new User();
        user.setName("razik");
        when(userRepo.findbyusername("razik")).thenReturn(user);
        when(tokenHashUtil.hash(anyString())).thenReturn("hashed-token");

        String rawToken = refreshTokenService.createRefreshToken("razik");

        assertNotNull(rawToken);
        assertNotEquals("hashed-token", rawToken);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertSame(user, saved.getUser());
        assertEquals("hashed-token", saved.getTokenHash());
        assertNotNull(saved.getExpiryDate());
    }

    @Test
    void shouldThrowWhenCreatingTokenForMissingUser() {
        when(userRepo.findbyusername("missing")).thenReturn(null);

        assertThrows(
                UserNotFoundException.class,
                () -> refreshTokenService.createRefreshToken("missing")
        );

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void shouldFindRefreshTokenByHashOfRawToken() {
        RefreshToken refreshToken = new RefreshToken();
        when(tokenHashUtil.hash("raw-token")).thenReturn("hashed-token");
        when(refreshTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByRawToken("raw-token");

        assertTrue(result.isPresent());
        assertSame(refreshToken, result.get());
    }

    @Test
    void shouldThrowWhenRawTokenDoesNotMatchStoredHash() {
        when(tokenHashUtil.hash("bad-token")).thenReturn("bad-hash");
        when(refreshTokenRepository.findByTokenHash("bad-hash"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.findByRawToken("bad-token")
        );
    }

    @Test
    void shouldDeleteRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();

        refreshTokenService.deleteRefreshToken(refreshToken);

        verify(refreshTokenRepository).delete(refreshToken);
    }
}
