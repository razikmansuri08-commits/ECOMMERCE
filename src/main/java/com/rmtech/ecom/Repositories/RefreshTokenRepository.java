package com.rmtech.ecom.Repositories;

import com.rmtech.ecom.Entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

@Query
("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash")
    Optional<RefreshToken>
    findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
    UPDATE RefreshToken rt
    SET rt.usedAt = :usedAt
    WHERE rt.id = :id
      AND rt.usedAt IS NULL
""")
    int consumeToken(
            @Param("id") Long id,
            @Param("usedAt") LocalDateTime usedAt

    );
}

