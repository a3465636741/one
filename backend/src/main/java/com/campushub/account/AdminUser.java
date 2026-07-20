package com.campushub.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(nullable = false, length = 40)
    private String role;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    protected AdminUser() {
    }

    public AdminUser(
            String username,
            String passwordHash,
            String displayName,
            String role,
            boolean passwordChangeRequired
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public void recordLogin(LocalDateTime loginTime) {
        this.lastLoginAt = loginTime;
        this.updatedAt = loginTime;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.passwordChangeRequired = false;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
