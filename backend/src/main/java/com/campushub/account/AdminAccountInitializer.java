package com.campushub.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String displayName;

    public AdminAccountInitializer(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.initial-admin.username}") String username,
            @Value("${app.security.initial-admin.password}") String password,
            @Value("${app.security.initial-admin.display-name}") String displayName
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }
        if (username.isBlank() || password.length() < 10 || displayName.isBlank()) {
            throw new IllegalStateException("初始管理员账号配置无效，密码至少需要 10 位");
        }
        adminUserRepository.save(new AdminUser(
                username.trim(),
                passwordEncoder.encode(password),
                displayName.trim(),
                "ADMIN",
                true
        ));
        log.warn("已创建初始管理员账号 {}，首次登录后必须修改密码", username);
    }
}
