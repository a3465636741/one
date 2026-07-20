package com.campushub.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"category", "organizer", "entries"})
    List<Event> findAllByOrderByStartTimeAsc();

    @EntityGraph(attributePaths = {"category", "organizer", "entries"})
    List<Event> findAllByStatusOrderByStartTimeAsc(EventStatus status);

    @EntityGraph(attributePaths = {"category", "organizer", "entries"})
    List<Event> findAllByOrganizerIdOrderByStartTimeAsc(Long organizerId);

    @Override
    @EntityGraph(attributePaths = {"category", "organizer", "entries"})
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = {"category", "organizer", "entries"})
    Optional<Event> findByIdAndOrganizerId(Long id, Long organizerId);

    long countByCategoryId(Long categoryId);

    @Query("""
            SELECT e FROM Event e
            JOIN FETCH e.category c
            JOIN FETCH e.organizer o
            WHERE e.status = :status
              AND (:categorySlug IS NULL OR c.slug = :categorySlug)
              AND (:keyword IS NULL
                   OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:fromTime IS NULL OR e.startTime >= :fromTime)
              AND (:toTime IS NULL OR e.startTime < :toTime)
              AND (:includeExpired = TRUE OR e.endTime >= :now)
            ORDER BY e.startTime ASC
            """)
    List<Event> searchPublished(
            @Param("status") EventStatus status,
            @Param("keyword") String keyword,
            @Param("categorySlug") String categorySlug,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("includeExpired") boolean includeExpired,
            @Param("now") LocalDateTime now
    );

    @EntityGraph(attributePaths = {"category", "organizer", "entries"})
    Optional<Event> findByIdAndStatus(Long id, EventStatus status);
}
