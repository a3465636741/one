package com.campushub.event;

import com.campushub.category.Category;
import com.campushub.category.CategoryRepository;
import com.campushub.category.CategoryResponse;
import com.campushub.organizer.Organizer;
import com.campushub.organizer.OrganizerRepository;
import com.campushub.organizer.OrganizerResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizerRepository organizerRepository;

    public AdminEventService(
            EventRepository eventRepository,
            CategoryRepository categoryRepository,
            OrganizerRepository organizerRepository
    ) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.organizerRepository = organizerRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminEventResponse> findAll(EventStatus status) {
        List<Event> events = status == null
                ? eventRepository.findAllByOrderByStartTimeAsc()
                : eventRepository.findAllByStatusOrderByStartTimeAsc(status);
        return events.stream().map(AdminEventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AdminEventResponse findById(Long id) {
        return AdminEventResponse.from(requireEvent(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrganizerResponse> findOrganizers() {
        return organizerRepository.findAllByOrderByNameAsc()
                .stream()
                .map(OrganizerResponse::from)
                .toList();
    }

    public AdminEventResponse create(AdminEventRequest request) {
        validateRequest(request);
        Category category = requireCategory(request.categoryId());
        Organizer organizer = requireOrganizer(request.organizerId());

        Event event = new Event(
                request.title().trim(),
                request.summary().trim(),
                request.content().trim(),
                category,
                organizer,
                request.startTime(),
                request.endTime(),
                request.location().trim(),
                normalize(request.coverUrl()),
                normalize(request.sourceUrl()),
                EventStatus.DRAFT,
                request.demo()
        );
        event.replaceEntries(toEntries(event, request.entries()));
        return AdminEventResponse.from(eventRepository.save(event));
    }

    public AdminEventResponse update(Long id, AdminEventRequest request) {
        validateRequest(request);
        Event event = requireEvent(id);
        event.update(
                request.title().trim(),
                request.summary().trim(),
                request.content().trim(),
                requireCategory(request.categoryId()),
                requireOrganizer(request.organizerId()),
                request.startTime(),
                request.endTime(),
                request.location().trim(),
                normalize(request.coverUrl()),
                normalize(request.sourceUrl()),
                request.demo()
        );
        event.replaceEntries(toEntries(event, request.entries()));
        return AdminEventResponse.from(event);
    }

    public AdminEventResponse changeStatus(Long id, EventStatus status) {
        Event event = requireEvent(id);
        if (status == EventStatus.PUBLISHED && event.getEntries().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "发布活动前必须配置官方入口");
        }
        event.changeStatus(status);
        return AdminEventResponse.from(event);
    }

    private void validateRequest(AdminEventRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
        for (AdminEventEntryRequest entry : request.entries()) {
            if (normalize(entry.url()) == null && normalize(entry.qrImageUrl()) == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "每个官方入口至少需要链接或二维码图片"
                );
            }
        }
    }

    private List<EventEntry> toEntries(Event event, List<AdminEventEntryRequest> entries) {
        return entries.stream()
                .map(entry -> new EventEntry(
                        event,
                        entry.type(),
                        entry.label().trim(),
                        normalize(entry.url()),
                        normalize(entry.qrImageUrl()),
                        entry.sortOrder()
                ))
                .toList();
    }

    private Event requireEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
    }

    private Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动分类不存在"));
    }

    private Organizer requireOrganizer(Long id) {
        return organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "主办方不存在"));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
