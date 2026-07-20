package com.campushub.config;

import com.campushub.account.AdminUserRepository;
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
    private final ObjectMapper objectMapper;

    public PasswordChangeRequiredFilter(
            AdminUserRepository adminUserRepository,
            ObjectMapper objectMapper
    ) {
        this.adminUserRepository = adminUserRepository;
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

        filterChain.doFilter(request, response);
    }
}
