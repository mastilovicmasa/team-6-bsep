package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.issuerCa = :issuerCa")
    List<User> findAllByIssuerCa(@Param("issuerCa") CertificateAuthority issuerCa);
    Optional<User> findByCertificateAuthority(CertificateAuthority cert);


}
