package com.campushub.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AdminAccountService adminAccountService;

    public AdminAuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            AdminAccountService adminAccountService
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.adminAccountService = adminAccountService;
    }

    @GetMapping("/auth/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @PostMapping("/auth/login")
    public AdminAccountResponse login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(),
                            request.password()
                    )
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            return adminAccountService.recordLogin(authentication.getName());
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码不正确");
        }
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    @GetMapping("/account")
    public AdminAccountResponse account(Principal principal) {
        return adminAccountService.account(principal.getName());
    }

    @PatchMapping("/account/password")
    public AdminAccountResponse changePassword(
            Principal principal,
            @Valid @RequestBody AdminPasswordChangeRequest request
    ) {
        return adminAccountService.changePassword(principal.getName(), request);
    }
}
