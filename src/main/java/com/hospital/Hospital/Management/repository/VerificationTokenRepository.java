package com.hospital.Hospital.Management.repository;

import com.hospital.Hospital.Management.model.TokenType;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndTokenType(User user, TokenType tokenType);
}
