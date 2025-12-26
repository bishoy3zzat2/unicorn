package com.unicorn.backend.report;

import com.unicorn.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for report management.
 * Provides endpoints for users to submit reports and admins to manage them.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ==================== USER ENDPOINTS ====================

    /**
     * Report a user.
     * POST /api/v1/reports/user/{userId}
     */
    @PostMapping("/reports/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> reportUser(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User reporter) {

        Report report = reportService.createReport(
                reporter.getId(),
                ReportedEntityType.USER,
                userId,
                request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Report submitted successfully. Our team will review it shortly.");
        response.put("reportId", report.getId());
        response.put("status", report.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * Report a startup.
     * POST /api/v1/reports/startup/{startupId}
     */
    @PostMapping("/reports/startup/{startupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> reportStartup(
            @PathVariable UUID startupId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User reporter) {

        Report report = reportService.createReport(
                reporter.getId(),
                ReportedEntityType.STARTUP,
                startupId,
                request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Report submitted successfully. Our team will review it shortly.");
        response.put("reportId", report.getId());
        response.put("status", report.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * Report a post.
     * POST /api/v1/reports/post/{postId}
     */
    @PostMapping("/reports/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> reportPost(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User reporter) {

        Report report = reportService.createReport(
                reporter.getId(),
                ReportedEntityType.POST,
                postId,
                request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post reported successfully. Our team will review it shortly.");
        response.put("reportId", report.getId());
        response.put("status", report.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * Report a comment.
     * POST /api/v1/reports/comment/{commentId}
     */
    @PostMapping("/reports/comment/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> reportComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User reporter) {

        Report report = reportService.createReport(
                reporter.getId(),
                ReportedEntityType.COMMENT,
                commentId,
                request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Comment reported successfully. Our team will review it shortly.");
        response.put("reportId", report.getId());
        response.put("status", report.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's reports.
     * GET /api/v1/reports/my-reports
     */
    @GetMapping("/reports/my-reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReportResponse>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReportResponse> reports = reportService.getReportsByReporter(user.getId(), pageable);

        return ResponseEntity.ok(reports);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all reports (admin).
     * GET /api/v1/admin/reports
     */
    @GetMapping("/admin/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<ReportResponse>> getAllReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportedEntityType entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ReportResponse> reports;
        if (status != null) {
            reports = reportService.getReportsByStatus(status, pageable);
        } else if (entityType != null) {
            reports = reportService.getReportsByEntityType(entityType, pageable);
        } else {
            reports = reportService.getAllReports(pageable);
        }

        return ResponseEntity.ok(reports);
    }

    /**
     * Get report details (admin).
     * GET /api/v1/admin/reports/{id}
     */
    @GetMapping("/admin/reports/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ReportResponse> getReportDetails(@PathVariable UUID id) {
        ReportResponse report = reportService.getReportDetailsById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * Update report status (admin).
     * PUT /api/v1/admin/reports/{id}/status
     */
    @PutMapping("/admin/reports/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Report> updateReportStatus(
            @PathVariable UUID id,
            @RequestParam ReportStatus status,
            @AuthenticationPrincipal User admin) {

        Report report = reportService.updateReportStatus(id, status, admin.getId());
        return ResponseEntity.ok(report);
    }

    /**
     * Resolve a report with action (admin).
     * POST /api/v1/admin/reports/{id}/resolve
     */
    @PostMapping("/admin/reports/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> resolveReport(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request,
            @AuthenticationPrincipal User admin) {

        Report report = reportService.resolveReport(id, request, admin.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Report resolved successfully");
        response.put("report", report);

        return ResponseEntity.ok(response);
    }

    /**
     * Reject a report as false (admin).
     * POST /api/v1/admin/reports/{id}/reject
     */
    @PostMapping("/admin/reports/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectReport(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User admin) {

        Report report = reportService.rejectReport(id, reason, admin.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Report rejected");
        response.put("report", report);

        return ResponseEntity.ok(response);
    }

    /**
     * Warn a reporter about false reports (admin).
     * POST /api/v1/admin/reports/{id}/warn-reporter
     */
    @PostMapping("/admin/reports/{id}/warn-reporter")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> warnReporter(
            @PathVariable UUID id,
            @RequestParam String warningMessage) {

        // Use getReportById (internal) to get entity for getting reporterId
        Report report = reportService.getReportById(id);
        reportService.warnReporter(report.getReporterId(), warningMessage);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Warning sent to reporter");

        return ResponseEntity.ok(response);
    }

    /**
     * Restrict a reporter from submitting reports (admin).
     * POST /api/v1/admin/reports/{id}/restrict-reporter
     */
    @PostMapping("/admin/reports/{id}/restrict-reporter")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> restrictReporter(
            @PathVariable UUID id,
            @RequestParam String reason) {

        Report report = reportService.getReportById(id);
        reportService.restrictReporter(report.getReporterId(), reason);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reporter restricted from submitting reports");

        return ResponseEntity.ok(response);
    }

    /**
     * Get reporter statistics (admin).
     * GET /api/v1/admin/reporters/{userId}/statistics
     */
    @GetMapping("/admin/reporters/{userId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ReporterStatistics> getReporterStatistics(@PathVariable UUID userId) {
        ReporterStatistics stats = reportService.getReporterStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get report counts by status (admin).
     * GET /api/v1/admin/reports/stats
     */
    @GetMapping("/admin/reports/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Long>> getReportStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", reportService.getReportCountByStatus(null));
        stats.put("pending", reportService.getReportCountByStatus(ReportStatus.PENDING));
        stats.put("underReview", reportService.getReportCountByStatus(ReportStatus.UNDER_REVIEW));
        stats.put("resolved", reportService.getReportCountByStatus(ReportStatus.RESOLVED));
        stats.put("rejected", reportService.getReportCountByStatus(ReportStatus.REJECTED));

        return ResponseEntity.ok(stats);
    }

    /**
     * Get reports for a specific entity (admin).
     * GET /api/v1/admin/reports/entity/{entityType}/{entityId}
     */
    @GetMapping("/admin/reports/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ReportResponse>> getReportsForEntity(
            @PathVariable ReportedEntityType entityType,
            @PathVariable UUID entityId) {

        List<ReportResponse> reports = reportService.getReportsForEntity(entityType, entityId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Delete a report (admin).
     * DELETE /api/v1/admin/reports/{id}
     */
    @DeleteMapping("/admin/reports/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable UUID id) {
        reportService.deleteReport(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Report deleted successfully");

        return ResponseEntity.ok(response);
    }
}
