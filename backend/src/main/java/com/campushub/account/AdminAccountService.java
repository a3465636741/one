package com.campushub.account;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminAccountService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AdminUser requireUser(String username) {
        return adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "管理员账号不存在"
                ));
    }

    @Transactional(readOnly = true)
    public AdminAccountResponse account(String username) {
        return AdminAccountResponse.from(requireUser(username));
    }

    public AdminAccountResponse recordLogin(String username) {
        AdminUser user = requireUser(username);
        user.recordLogin(LocalDateTime.now());
        return AdminAccountResponse.from(user);
    }

    public AdminAccountResponse changePassword(
            String username,
            AdminPasswordChangeRequest request
    ) {
        AdminUser user = requireUser(username);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return AdminAccountResponse.from(user);
    }
}
