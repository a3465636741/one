package com.campushub.event;

import com.campushub.category.CategoryRepository;
import com.campushub.category.CategoryResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class EventCatalogService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public EventCatalogService(
            EventRepository eventRepository,
            CategoryRepository categoryRepository
    ) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> findCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<EventSummaryResponse> search(
            String keyword,
            String categorySlug,
            LocalDate from,
            LocalDate to
    ) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "结束日期不能早于开始日期");
        }

        LocalDateTime fromTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toTime = to == null ? null : to.plusDays(1).atStartOfDay();

        return eventRepository.searchPublished(
                        EventStatus.PUBLISHED,
                        normalize(keyword),
                        normalize(categorySlug),
                        fromTime,
                        toTime
                )
                .stream()
                .map(EventSummaryResponse::from)
                .toList();
    }

    public EventDetailResponse findById(Long id) {
        return eventRepository.findByIdAndStatus(id, EventStatus.PUBLISHED)
                .map(EventDetailResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
