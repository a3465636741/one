package com.campushub.organizer;

import com.campushub.category.Category;
import com.campushub.category.CategoryRepository;
import com.campushub.category.CategoryResponse;
import com.campushub.event.AdminEventEntryRequest;
import com.campushub.event.AdminEventResponse;
import com.campushub.event.Event;
import com.campushub.event.EventEntry;
import com.campushub.event.EventRepository;
import com.campushub.event.EventStatus;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OrganizerEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizerRepository organizerRepository;

    public OrganizerEventService(
            EventRepository eventRepository,
            CategoryRepository categoryRepository,
            OrganizerRepository organizerRepository
    ) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.organizerRepository = organizerRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminEventResponse> findAll(Long organizerId) {
        return eventRepository.findAllByOrganizerIdOrderByStartTimeAsc(organizerId)
                .stream()
                .map(AdminEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public AdminEventResponse create(Long organizerId, OrganizerEventRequest request) {
        validateRequest(request);
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "关联的活动方不存在"
                ));
        Event event = new Event(
                request.title().trim(),
                request.summary().trim(),
                request.content().trim(),
                requireCategory(request.categoryId()),
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

    public AdminEventResponse update(
            Long organizerId,
            Long eventId,
            OrganizerEventRequest request
    ) {
        validateRequest(request);
        Event event = requireOwnedEvent(organizerId, eventId);
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "只有草稿或已退回活动可以修改"
            );
        }
        event.update(
                request.title().trim(),
                request.summary().trim(),
                request.content().trim(),
                requireCategory(request.categoryId()),
                event.getOrganizer(),
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

    public AdminEventResponse submit(Long organizerId, Long eventId) {
        Event event = requireOwnedEvent(organizerId, eventId);
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "当前状态不能再次提交审核");
        }
        if (normalize(event.getSourceUrl()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提交审核前必须填写信息来源");
        }
        if (event.getEntries().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提交审核前必须配置官方入口");
        }
        event.changeStatus(EventStatus.PENDING_REVIEW, null);
        return AdminEventResponse.from(event);
    }

    public AdminEventResponse cancel(Long organizerId, Long eventId) {
        Event event = requireOwnedEvent(organizerId, eventId);
        if (event.getStatus() != EventStatus.PUBLISHED
                && event.getStatus() != EventStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "只有待审核或已发布活动可以取消"
            );
        }
        event.changeStatus(EventStatus.CANCELLED, null);
        return AdminEventResponse.from(event);
    }

    private void validateRequest(OrganizerEventRequest request) {
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

    private Event requireOwnedEvent(Long organizerId, Long eventId) {
        return eventRepository.findByIdAndOrganizerId(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "活动不存在或不属于当前活动方"
                ));
    }

    private Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "活动分类不存在"
                ));
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

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
