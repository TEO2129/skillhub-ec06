package com.example.auth.repository;

import com.example.auth.entity.Nonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NonceRepository extends JpaRepository<Nonce, Long> {
    Optional<Nonce> findByNonce(String nonce);
    void deleteByExpiresAtBefore(LocalDateTime date);
}