package com.campushub.organizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrganizerAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OrganizerAccountInitializer.class);

    private final OrganizerUserRepository organizerUserRepository;
    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;
    private final Long organizerId;
    private final String username;
    private final String password;
    private final String displayName;

    public OrganizerAccountInitializer(
            OrganizerUserRepository organizerUserRepository,
            OrganizerRepository organizerRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.initial-organizer.organizer-id:0}") Long organizerId,
            @Value("${app.security.initial-organizer.username:}") String username,
            @Value("${app.security.initial-organizer.password:}") String password,
            @Value("${app.security.initial-organizer.display-name:}") String displayName
    ) {
        this.organizerUserRepository = organizerUserRepository;
        this.organizerRepository = organizerRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizerId = organizerId;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        if (organizerUserRepository.findByUsername(username.trim()).isPresent()) {
            return;
        }
        if (organizerId <= 0 || password.length() < 10 || displayName.isBlank()) {
            throw new IllegalStateException("初始活动方账号配置无效，密码至少需要 10 位");
        }
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalStateException("初始活动方关联的主办方不存在"));
        organizerUserRepository.save(new OrganizerUser(
                organizer,
                username.trim(),
                passwordEncoder.encode(password),
                displayName.trim()
        ));
        log.warn("已创建演示活动方账号 {}，首次登录后必须修改密码", username);
    }
}
