package com.rmtech.ecom.Service;

import com.rmtech.ecom.Config.TokenHashUtil;
import com.rmtech.ecom.DTOS.Error_ResponseDto;
import com.rmtech.ecom.Entities.RefreshToken;
import com.rmtech.ecom.Entities.User;
import com.rmtech.ecom.Exception.JwtTokenInvalidException;
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


    public RefreshTokenService(User_Repo ur, RefreshTokenRepository refreshTokenRepository, TokenHashUtil tokenHashUtil) {
        this.ur = ur;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHashUtil = tokenHashUtil;
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




    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }


}
