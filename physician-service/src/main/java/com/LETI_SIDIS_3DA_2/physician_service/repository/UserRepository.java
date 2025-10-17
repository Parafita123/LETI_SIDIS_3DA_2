package com.LETI_SIDIS_3DA_2.physician_service.repository;

import com.LETI_SIDIS_3DA_2.physician_service.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
