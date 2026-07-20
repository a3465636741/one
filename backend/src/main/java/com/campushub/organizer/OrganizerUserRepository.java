package com.campushub.organizer;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerUserRepository extends JpaRepository<OrganizerUser, Long> {

    @EntityGraph(attributePaths = "organizer")
    Optional<OrganizerUser> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = "organizer")
    Optional<OrganizerUser> findById(Long id);
}
