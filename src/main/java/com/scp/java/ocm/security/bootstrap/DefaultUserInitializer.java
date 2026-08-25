package com.scp.java.ocm.security.bootstrap;

import com.scp.java.ocm.security.entity.AppUser;
import com.scp.java.ocm.security.entity.Role;
import com.scp.java.ocm.security.repository.AppUserRepository;
import java.util.Collections;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserInitializer.class);
    private final AppUserRepository repository;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String careManagerPassword;
    private final String viewerPassword;

    public DefaultUserInitializer(
            AppUserRepository repository, PasswordEncoder encoder, Environment environment) {
        this.repository = repository;
        this.encoder = encoder;
        this.adminPassword = environment.getProperty("ocm.bootstrap.admin-password", "Admin@12345");
        this.careManagerPassword =
                environment.getProperty("ocm.bootstrap.care-manager-password", "Care@12345");
        this.viewerPassword = environment.getProperty("ocm.bootstrap.viewer-password", "Viewer@12345");
    }

    @Override
    public void run(ApplicationArguments args) {
        create("admin", adminPassword, "OCM Administrator", Role.ADMIN);
        create("care.manager", careManagerPassword, "Care Manager", Role.CARE_MANAGER);
        create("viewer", viewerPassword, "Read Only Viewer", Role.VIEWER);
        LOGGER.warn("OCM bootstrap defaults are for development only; change them outside dev.");
    }

    private void create(String username, String password, String name, Role role) {
        if (!repository.existsByUsernameIgnoreCase(username)) {
            repository.save(
                    new AppUser(
                            username,
                            encoder.encode(password),
                            name,
                            new HashSet<Role>(Collections.singleton(role))));
        }
    }
}
