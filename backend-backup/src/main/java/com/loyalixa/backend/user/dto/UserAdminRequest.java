package com.loyalixa.backend.user.dto;

import java.time.LocalDateTime;

public record UserAdminRequest(
        // الدور الجديد (مثلاً: INSTRUCTOR, ADMIN) - Optional: إذا لم يتم التحديد، يتم
        // الحفاظ على الدور الحالي
        String newRoleName,

        // سبب الإجراء (مهم لسجلات المراجعة)
        String actionReason,

        // حالة المستخدم الجديدة (ACTIVE, SUSPENDED, BANNED)
        String newStatus,

        // نوع التعليق/الحظر: PERMANENT (دائم) أو TEMPORARY (مؤقت)
        String suspensionType, // PERMANENT, TEMPORARY (للساسبند)
        String banType, // PERMANENT, TEMPORARY (للبان)

        // تاريخ انتهاء التعليق/الحظر (null = دائم)
        LocalDateTime suspendedUntil, // للتعليق المؤقت
        LocalDateTime bannedUntil, // للحظر المؤقت

        // [جديد] - صلاحية الوصول للداشبورد الإداري
        Boolean canAccessDashboard) {
}