package com.unicorn.backend.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ChatReport entity operations.
 */
@Repository
public interface ChatReportRepository extends JpaRepository<ChatReport, UUID> {

    /**
     * Find all reports with a specific status (for admin moderation).
     */
    Page<ChatReport> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    /**
     * Find all reports (paginated, for admin dashboard).
     */
    Page<ChatReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find reports for a specific message.
     */
    List<ChatReport> findByMessage(ChatMessage message);

    /**
     * Count pending reports.
     */
    long countByStatus(ReportStatus status);
}
