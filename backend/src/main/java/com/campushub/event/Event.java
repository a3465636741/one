package com.campushub.event;

import com.campushub.category.Category;
import com.campushub.organizer.Organizer;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EventStatus status;

    @Column(nullable = false)
    private boolean demo;

    @OneToMany(
            mappedBy = "event",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder ASC")
    private List<EventEntry> entries = new ArrayList<>();

    protected Event() {
    }

    public Event(
            String title,
            String summary,
            String content,
            Category category,
            Organizer organizer,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String location,
            String coverUrl,
            String sourceUrl,
            EventStatus status,
            boolean demo
    ) {
        update(
                title,
                summary,
                content,
                category,
                organizer,
                startTime,
                endTime,
                location,
                coverUrl,
                sourceUrl,
                demo
        );
        this.status = status;
    }

    public void update(
            String title,
            String summary,
            String content,
            Category category,
            Organizer organizer,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String location,
            String coverUrl,
            String sourceUrl,
            boolean demo
    ) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.category = category;
        this.organizer = organizer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.coverUrl = coverUrl;
        this.sourceUrl = sourceUrl;
        this.demo = demo;
    }

    public void replaceEntries(List<EventEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
    }

    public void changeStatus(EventStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public Category getCategory() {
        return category;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public EventStatus getStatus() {
        return status;
    }

    public boolean isDemo() {
        return demo;
    }

    public List<EventEntry> getEntries() {
        return List.copyOf(entries);
    }
}
