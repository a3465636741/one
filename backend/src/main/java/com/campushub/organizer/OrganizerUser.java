package com.campushub.organizer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizer_users")
public class OrganizerUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    protected OrganizerUser() {
    }

    public OrganizerUser(
            Organizer organizer,
            String username,
            String passwordHash,
            String displayName
    ) {
        this.organizer = organizer;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.passwordChangeRequired = true;
        this.active = true;
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

    public Organizer getOrganizer() {
        return organizer;
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

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
