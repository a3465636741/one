package com.campushub.config;

import com.campushub.account.AdminUserRepository;
import com.campushub.organizer.OrganizerPrincipal;
import com.campushub.organizer.OrganizerUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    private final AdminUserRepository adminUserRepository;
    private final OrganizerUserRepository organizerUserRepository;
    private final ObjectMapper objectMapper;

    public PasswordChangeRequiredFilter(
            AdminUserRepository adminUserRepository,
            OrganizerUserRepository organizerUserRepository,
            ObjectMapper objectMapper
    ) {
        this.adminUserRepository = adminUserRepository;
        this.organizerUserRepository = organizerUserRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean protectedAdminOperation = path.startsWith("/api/admin/")
                && !path.startsWith("/api/admin/auth/")
                && !path.startsWith("/api/admin/account");
        boolean protectedOrganizerOperation = path.startsWith("/api/organizer/")
                && !path.startsWith("/api/organizer/auth/")
                && !path.startsWith("/api/organizer/account");

        if (protectedAdminOperation
                && authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            boolean passwordChangeRequired = adminUserRepository
                    .findByUsername(authentication.getName())
                    .map(user -> user.isPasswordChangeRequired())
                    .orElse(false);
            if (passwordChangeRequired) {
                response.setStatus(428);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(), Map.of(
                        "message", "首次登录后请先修改初始密码"
                ));
                return;
            }
        }

        if (protectedOrganizerOperation
                && authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof OrganizerPrincipal principal) {
            boolean passwordChangeRequired = organizerUserRepository
                    .findById(principal.userId())
                    .map(user -> user.isPasswordChangeRequired())
                    .orElse(false);
            if (passwordChangeRequired) {
                response.setStatus(428);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(), Map.of(
                        "message", "首次登录后请先修改初始密码"
                ));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
