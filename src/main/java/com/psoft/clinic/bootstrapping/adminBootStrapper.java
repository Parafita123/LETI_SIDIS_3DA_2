package com.psoft.clinic.bootstrapping;

import com.psoft.clinic.adminmanagement.services.AdminService;
import com.psoft.clinic.adminmanagement.services.CreateAdminRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("bootstrap")
@RequiredArgsConstructor
@Order(4)
public class adminBootStrapper implements CommandLineRunner {

    private final AdminService adminService;

    @Override
    @Transactional
    public void run(String... args) {
        createIfNotExists("admin1", "Password1!", "Administrator One");
        createIfNotExists("admin2", "Password2!", "Administrator Two");
        createIfNotExists("admin3", "Password3!", "Administrator Three");
    }

    private void createIfNotExists(String username, String password, String fullName) {
        if (adminService.findByUsername(username).isEmpty()) {
            CreateAdminRequest req = new CreateAdminRequest(
                    username,
                    password,
                    fullName
            );
            adminService.create(req);
        }
    }
}
