package com.campushub.organizer;

import com.campushub.account.CsrfTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer")
public class OrganizerAuthController {

    private final OrganizerAccountService organizerAccountService;
    private final SecurityContextRepository securityContextRepository;

    public OrganizerAuthController(
            OrganizerAccountService organizerAccountService,
            SecurityContextRepository securityContextRepository
    ) {
        this.organizerAccountService = organizerAccountService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/auth/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @PostMapping("/auth/login")
    public OrganizerAccountResponse login(
            @Valid @RequestBody OrganizerLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        OrganizerUser user = organizerAccountService.authenticate(
                request.username(),
                request.password()
        );
        OrganizerPrincipal principal = new OrganizerPrincipal(
                user.getId(),
                user.getOrganizer().getId(),
                user.getUsername(),
                user.getDisplayName()
        );
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return OrganizerAccountResponse.from(user);
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
    public OrganizerAccountResponse account(Authentication authentication) {
        return organizerAccountService.account(principal(authentication).userId());
    }

    @PatchMapping("/account/password")
    public OrganizerAccountResponse changePassword(
            Authentication authentication,
            @Valid @RequestBody OrganizerPasswordChangeRequest request
    ) {
        return organizerAccountService.changePassword(
                principal(authentication).userId(),
                request
        );
    }

    private OrganizerPrincipal principal(Authentication authentication) {
        return (OrganizerPrincipal) authentication.getPrincipal();
    }
}
