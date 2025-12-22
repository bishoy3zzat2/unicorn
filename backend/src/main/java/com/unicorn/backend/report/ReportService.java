package com.unicorn.backend.report;

import com.unicorn.backend.startup.StartupRepository;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing reports and reporter statistics.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReporterStatisticsRepository reporterStatisticsRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;

    // Auto-warn thresholds
    private static final float AUTO_WARN_THRESHOLD = 0.5f; // 50%
    private static final int MIN_REPORTS_FOR_AUTO_WARN = 5;

    // Auto-restrict thresholds
    private static final float AUTO_RESTRICT_THRESHOLD = 0.7f; // 70%
    private static final int MIN_REPORTS_FOR_AUTO_RESTRICT = 10;

    /**
     * Create a new report.
     */
    @Transactional
    public Report createReport(
            UUID reporterId,
            ReportedEntityType entityType,
            UUID entityId,
            CreateReportRequest request) {
        // Validate reporter is not restricted
        ReporterStatistics stats = getOrCreateReporterStatistics(reporterId);
        if (Boolean.TRUE.equals(stats.getReportingRestricted())) {
            throw new IllegalStateException(
                    "You are restricted from submitting reports. Reason: " + stats.getRestrictionReason());
        }

        // Validate reporter is not reporting themselves
        if (entityType == ReportedEntityType.USER && entityId.equals(reporterId)) {
            throw new IllegalArgumentException("You cannot report yourself");
        }

        // Check for duplicate reports (same reporter, entity, and pending/under review
        // status)
        List<ReportStatus> activeStatuses = List.of(ReportStatus.PENDING, ReportStatus.UNDER_REVIEW);
        boolean duplicateExists = reportRepository
                .existsByReporterIdAndReportedEntityTypeAndReportedEntityIdAndStatusIn(
                        reporterId, entityType, entityId, activeStatuses);
        if (duplicateExists) {
            throw new IllegalArgumentException("You have already reported this " + entityType.toString().toLowerCase());
        }

        // Validate reported entity exists
        validateEntityExists(entityType, entityId);

        // Create report
        Report report = Report.builder()
                .reporterId(reporterId)
                .reportedEntityType(entityType)
                .reportedEntityId(entityId)
                .reason(request.reason())
                .description(request.description())
                .status(ReportStatus.PENDING)
                .notifyReporter(true)
                .reporterNotified(false)
                .build();

        Report savedReport = reportRepository.save(report);

        // Update reporter statistics
        stats.incrementTotalReports();
        reporterStatisticsRepository.save(stats);

        return savedReport;
    }

    /**
     * Get or create reporter statistics for a user.
     */
    private ReporterStatistics getOrCreateReporterStatistics(UUID userId) {
        return reporterStatisticsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    ReporterStatistics stats = ReporterStatistics.builder()
                            .userId(userId)
                            .build();
                    return reporterStatisticsRepository.save(stats);
                });
    }

    /**
     * Validate that the reported entity exists.
     */
    private void validateEntityExists(ReportedEntityType entityType, UUID entityId) {
        switch (entityType) {
            case USER:
                if (!userRepository.existsById(entityId)) {
                    throw new IllegalArgumentException("User not found");
                }
                break;
            case STARTUP:
                if (!startupRepository.existsById(entityId)) {
                    throw new IllegalArgumentException("Startup not found");
                }
                break;
            // Future entity types can be added here
        }
    }

    /**
     * Get report entity by ID (internal use).
     */
    public Report getReportById(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    }

    /**
     * Get report details DTO by ID.
     */
    public ReportResponse getReportDetailsById(UUID reportId) {
        Report report = getReportById(reportId);
        return mapToResponse(report);
    }

    /**
     * Get all reports with pagination (DTOs).
     */
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::mapToResponse);
    }

    /**
     * Get reports by status (DTOs).
     */
    public Page<ReportResponse> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    /**
     * Get reports by entity type (DTOs).
     */
    public Page<ReportResponse> getReportsByEntityType(ReportedEntityType entityType, Pageable pageable) {
        return reportRepository.findByReportedEntityType(entityType, pageable).map(this::mapToResponse);
    }

    /**
     * Get reports by reporter (DTOs).
     */
    public Page<ReportResponse> getReportsByReporter(UUID reporterId, Pageable pageable) {
        return reportRepository.findByReporterId(reporterId, pageable).map(this::mapToResponse);
    }

    /**
     * Get reports for a specific entity (DTOs).
     */
    public List<ReportResponse> getReportsForEntity(ReportedEntityType entityType, UUID entityId) {
        return reportRepository.findByReportedEntityTypeAndReportedEntityId(entityType, entityId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ReportResponse mapToResponse(Report report) {
        ReportResponse.ReportResponseBuilder builder = ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reportedEntityType(report.getReportedEntityType())
                .reportedEntityId(report.getReportedEntityId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .adminAction(report.getAdminAction())
                .adminId(report.getAdminId())
                .adminNotes(report.getAdminNotes())
                .actionDetails(report.getActionDetails())
                .notifyReporter(report.getNotifyReporter())
                .reporterNotified(report.getReporterNotified())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .resolvedAt(report.getResolvedAt());

        // Fetch Reporter
        userRepository.findById(report.getReporterId()).ifPresent(reporter -> {
            String name = (reporter.getDisplayName() != null && !reporter.getDisplayName().isEmpty())
                    ? reporter.getDisplayName()
                    : reporter.getFirstName() + " " + reporter.getLastName();
            builder.reporterName(name);
            builder.reporterImage(reporter.getAvatarUrl());
        });

        // Fetch Reported Entity
        if (report.getReportedEntityType() == ReportedEntityType.USER) {
            userRepository.findById(report.getReportedEntityId()).ifPresent(user -> {
                String name = (user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                        ? user.getDisplayName()
                        : user.getFirstName() + " " + user.getLastName();
                builder.reportedEntityName(name);
                builder.reportedEntityImage(user.getAvatarUrl());
            });
        } else if (report.getReportedEntityType() == ReportedEntityType.STARTUP) {
            startupRepository.findById(report.getReportedEntityId()).ifPresent(startup -> {
                builder.reportedEntityName(startup.getName());
                builder.reportedEntityImage(startup.getLogoUrl());
                builder.reportedEntityStatus(startup.getStatus().name());
            });
        }

        return builder.build();
    }

    /**
     * Update report status.
     */
    @Transactional
    public Report updateReportStatus(UUID reportId, ReportStatus newStatus, UUID adminId) {
        Report report = getReportById(reportId);
        report.setStatus(newStatus);
        report.setAdminId(adminId);

        if (newStatus == ReportStatus.RESOLVED && report.getResolvedAt() == null) {
            report.setResolvedAt(LocalDateTime.now());
        }

        return reportRepository.save(report);
    }

    /**
     * Get count of reports by status.
     * If status is null, returns total count of all reports.
     */
    public long getReportCountByStatus(ReportStatus status) {
        if (status == null) {
            return reportRepository.count();
        }
        return reportRepository.countByStatus(status);
    }

    /**
     * Get reporter statistics.
     */
    public ReporterStatistics getReporterStatistics(UUID userId) {
        return getOrCreateReporterStatistics(userId);
    }

    /**
     * Resolve a report with action.
     */
    @Transactional
    public Report resolveReport(UUID reportId, ResolveReportRequest request, UUID adminId) {
        Report report = getReportById(reportId);

        report.setStatus(ReportStatus.RESOLVED);
        report.setAdminAction(request.adminAction());
        report.setActionDetails(request.actionDetails());
        report.setAdminNotes(request.adminNotes());
        report.setAdminId(adminId);
        report.setResolvedAt(LocalDateTime.now());
        report.setNotifyReporter(request.notifyReporter() != null ? request.notifyReporter() : true);

        Report savedReport = reportRepository.save(report);

        // Update reporter statistics - increment resolved count
        ReporterStatistics stats = getOrCreateReporterStatistics(report.getReporterId());
        stats.incrementResolvedReports();
        reporterStatisticsRepository.save(stats);

        // TODO: Execute the actual admin action (warning, suspension, etc.)
        // Mock Notification Logic
        if (Boolean.TRUE.equals(request.notifyReporter())) {
            List<NotificationChannel> channels = request.reporterNotificationChannels() != null
                    ? request.reporterNotificationChannels()
                    : List.of(NotificationChannel.IN_APP); // Default
            System.out.println("Mock: Notifying User " + report.getReporterId() + " via " + channels);
            // TODO: Call NotificationService
        }
        if (Boolean.TRUE.equals(request.notifyReportedEntity())) {
            List<NotificationChannel> channels = request.reportedEntityNotificationChannels() != null
                    ? request.reportedEntityNotificationChannels()
                    : List.of(NotificationChannel.IN_APP); // Default
            System.out.println("Mock: Notifying Reported Entity " + report.getReportedEntityId() + " via " + channels);
            // TODO: Call NotificationService
        }

        return savedReport;
    }

    /**
     * Reject a report as false/invalid.
     */
    @Transactional
    public Report rejectReport(UUID reportId, String reason, UUID adminId) {
        Report report = getReportById(reportId);

        report.setStatus(ReportStatus.REJECTED);
        report.setAdminAction(AdminAction.NO_ACTION);
        report.setActionDetails("Report rejected as " + (reason != null ? reason : "invalid"));
        report.setAdminId(adminId);
        report.setResolvedAt(LocalDateTime.now());

        Report savedReport = reportRepository.save(report);

        // Update reporter statistics - increment rejected count
        ReporterStatistics stats = getOrCreateReporterStatistics(report.getReporterId());
        stats.incrementRejectedReports();
        reporterStatisticsRepository.save(stats);

        // Check if auto-warn or auto-restrict should be triggered
        checkAutoWarnOrRestrict(stats);

        return savedReport;
    }

    /**
     * Warn a reporter about false reports.
     */
    @Transactional
    public void warnReporter(UUID userId, String warningMessage) {
        ReporterStatistics stats = getOrCreateReporterStatistics(userId);
        stats.incrementWarningCount();
        reporterStatisticsRepository.save(stats);

        // TODO: Send warning email to reporter
    }

    /**
     * Restrict a reporter from submitting reports.
     */
    @Transactional
    public void restrictReporter(UUID userId, String reason) {
        ReporterStatistics stats = getOrCreateReporterStatistics(userId);
        stats.setReportingRestricted(true);
        stats.setRestrictedAt(LocalDateTime.now());
        stats.setRestrictionReason(reason);
        reporterStatisticsRepository.save(stats);

        // TODO: Send restriction notification to reporter
    }

    /**
     * Unrestrict a reporter (allow them to report again).
     */
    @Transactional
    public void unrestrictReporter(UUID userId) {
        ReporterStatistics stats = getOrCreateReporterStatistics(userId);
        stats.setReportingRestricted(false);
        stats.setRestrictedAt(null);
        stats.setRestrictionReason(null);
        reporterStatisticsRepository.save(stats);

        // TODO: Send unrestriction notification
    }

    /**
     * Check if reporter should be auto-warned or auto-restricted based on false
     * report rate.
     */
    private void checkAutoWarnOrRestrict(ReporterStatistics stats) {
        float falseReportRate = stats.getFalseReportRate();
        int totalReports = stats.getTotalReportsSubmitted();

        // Auto-restrict if false report rate is very high
        if (falseReportRate >= AUTO_RESTRICT_THRESHOLD && totalReports >= MIN_REPORTS_FOR_AUTO_RESTRICT) {
            if (!Boolean.TRUE.equals(stats.getReportingRestricted())) {
                restrictReporter(
                        stats.getUserId(),
                        String.format(
                                "Automatic restriction due to high false report rate: %.0f%% (%d/%d reports rejected)",
                                falseReportRate * 100,
                                stats.getRejectedReports(),
                                totalReports));
            }
        }
        // Auto-warn if false report rate is moderately high
        else if (falseReportRate >= AUTO_WARN_THRESHOLD && totalReports >= MIN_REPORTS_FOR_AUTO_WARN) {
            if (stats.getWarningCount() < 3) { // Limit automatic warnings
                warnReporter(
                        stats.getUserId(),
                        String.format(
                                "Warning: %d of your %d reports have been rejected. Please ensure your reports are valid to avoid restrictions.",
                                stats.getRejectedReports(),
                                totalReports));
            }
        }
    }
}
