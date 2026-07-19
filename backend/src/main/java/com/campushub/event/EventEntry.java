package com.campushub.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_entries")
public class EventEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EntryType type;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 500)
    private String url;

    @Column(name = "qr_image_url", length = 500)
    private String qrImageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected EventEntry() {
    }

    public Long getId() {
        return id;
    }

    public EntryType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public String getQrImageUrl() {
        return qrImageUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
