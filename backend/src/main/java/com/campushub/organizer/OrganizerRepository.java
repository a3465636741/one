package com.campushub.organizer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    List<Organizer> findAllByOrderByNameAsc();
}
