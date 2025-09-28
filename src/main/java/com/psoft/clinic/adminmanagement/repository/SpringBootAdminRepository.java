package com.psoft.clinic.adminmanagement.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.psoft.clinic.adminmanagement.model.Admin;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringBootAdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByBaseUserUsername(String username);
}
