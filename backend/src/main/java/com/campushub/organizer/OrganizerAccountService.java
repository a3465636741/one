package com.campushub.organizer;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OrganizerAccountService {

    private final OrganizerUserRepository organizerUserRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganizerAccountService(
            OrganizerUserRepository organizerUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.organizerUserRepository = organizerUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public OrganizerUser authenticate(String username, String password) {
        OrganizerUser user = organizerUserRepository.findByUsername(username.trim())
                .filter(OrganizerUser::isActive)
                .orElseThrow(this::badCredentials);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw badCredentials();
        }
        user.recordLogin(LocalDateTime.now());
        return user;
    }

    @Transactional(readOnly = true)
    public OrganizerAccountResponse account(Long userId) {
        return OrganizerAccountResponse.from(requireUser(userId));
    }

    public OrganizerAccountResponse changePassword(
            Long userId,
            OrganizerPasswordChangeRequest request
    ) {
        OrganizerUser user = requireUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return OrganizerAccountResponse.from(user);
    }

    private OrganizerUser requireUser(Long userId) {
        return organizerUserRepository.findById(userId)
                .filter(OrganizerUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "活动方账号不存在或已停用"
                ));
    }

    private ResponseStatusException badCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码不正确");
    }
}
