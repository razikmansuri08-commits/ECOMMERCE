package com.rmtech.ecom.Service;

import com.rmtech.ecom.Config.JwtUtil;
import com.rmtech.ecom.Config.TokenHashUtil;
import com.rmtech.ecom.DTOS.AuthResponse;
import com.rmtech.ecom.DTOS.Error_ResponseDto;
import com.rmtech.ecom.Entities.RefreshToken;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.JwtTokenInvalidException;
import com.rmtech.ecom.Exception.RefreshTokenException;
import com.rmtech.ecom.Exception.UserNotFoundException;
import com.rmtech.ecom.Repositories.RefreshTokenRepository;
import com.rmtech.ecom.Repositories.User_Repo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service

public class RefreshTokenService {
    private final User_Repo ur;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;
    private final JwtUtil jwtUtil;


    public RefreshTokenService(User_Repo ur, RefreshTokenRepository refreshTokenRepository, TokenHashUtil tokenHashUtil, JwtUtil jwtUtil) {
        this.ur = ur;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHashUtil = tokenHashUtil;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String createRefreshToken(String username) {
        User user=ur.findbyusername(username);
        if (user==null)
            throw new UserNotFoundException("user not found");
        String rawtoken=UUID.randomUUID().toString();
        RefreshToken refreshToken=new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHashUtil.hash(rawtoken));
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(5));
        refreshTokenRepository.save(refreshToken);
        return rawtoken;
    }
    public Optional<RefreshToken> findByRawToken(String token) {
        String hash =
                tokenHashUtil.hash(token);


        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid refresh token"
                        )
                );
        return Optional.of(refreshToken);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {

        String hash = tokenHashUtil.hash(rawRefreshToken);

        RefreshToken oldToken =
                refreshTokenRepository.findByTokenHash(hash)
                        .orElseThrow(() ->
                                new RefreshTokenException(
                                        "Invalid refresh token"
                                ));

        if (oldToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenException(
                    "Refresh token expired"
            );
        }

        int updated =
                refreshTokenRepository.consumeToken(
                        oldToken.getId(),
                        LocalDateTime.now()
                );

        if (updated != 1) {
            throw new RefreshTokenException(
                    "Refresh token already used"
            );
        }

        String newRawRefreshToken =
                UUID.randomUUID().toString();

        RefreshToken newToken = new RefreshToken();

        newToken.setUser(oldToken.getUser());

        newToken.setTokenHash(
                tokenHashUtil.hash(newRawRefreshToken)
        );

        newToken.setCreatedAt(LocalDateTime.now());

        newToken.setExpiryDate(
                LocalDateTime.now().plusDays(5)
        );

        String accessToken = jwtUtil.generateToken(oldToken.getUser().getName());

        oldToken.setReplacedby(newToken);
        refreshTokenRepository.save(newToken);



        return new AuthResponse(
                accessToken,
                newRawRefreshToken
        );
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }


}
