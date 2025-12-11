package com.loyalixa.backend.security;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "role_permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "role_id", "permission_id" }) // لمنع التكرار
})
// نستخدم المفتاح المركب
@IdClass(RolePermission.RolePermissionId.class)
public class RolePermission implements Serializable {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    // كلاس داخلي لتعريف المفتاح المركب
    @Data
    @NoArgsConstructor
    public static class RolePermissionId implements Serializable {
        private UUID role;
        private UUID permission;
    }
}