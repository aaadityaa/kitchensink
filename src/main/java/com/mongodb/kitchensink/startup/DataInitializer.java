package com.mongodb.kitchensink.startup;

import com.mongodb.kitchensink.model.UserInfo;
import com.mongodb.kitchensink.repository.UserInfoRepository;
import com.mongodb.kitchensink.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "app.bootstrap-admin", name = "enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UserInfoRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.name:Admin}")
    private String name;

    @Value("${app.bootstrap-admin.email:admin@example.com}")
    private String email;

    @Value("${app.bootstrap-admin.phone:0000000000}")
    private String phone;

    @Value("${app.bootstrap-admin.password:ChangeMe123!}")
    private String password;

    @Value("${app.bootstrap-admin.roles:ROLES_ADMIN}")
    private String roles;

    public DataInitializer(UserInfoRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        final String e = normEmail(email);
        if (e == null) {
            log.warn("Bootstrap admin skipped: email is missing.");
            return;
        }
        if (repo.findByEmail(e).isPresent()) {
            log.info("Bootstrap admin exists (email={}), skipping.", e);
            return;
        }

        UserInfo admin = new UserInfo();
        admin.setUsername(normText(name));
        admin.setEmail(e);
        admin.setPhone(normPhone(phone));
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRoles(normText(roles) != null ? roles : Constants.ROLES_ADMIN);

        repo.save(admin);
        log.info("Bootstrap admin created (id={}).", admin.getId());
    }

    private static String normEmail(String s) {
        return s == null ? null : s.trim().toLowerCase(Locale.ROOT);
    }
    private static String normPhone(String s) {
        return s == null ? null : s.replaceAll("\\D+", "");
    }
    private static String normText(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}