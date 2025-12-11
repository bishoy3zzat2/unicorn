package com.loyalixa.backend.security;
import com.loyalixa.backend.security.dto.RoleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/roles")
public class RoleAdminController {
    private final RoleAdminService roleAdminService;
    public RoleAdminController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('role:view_all')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleAdminService.getAllRoles());
    }
    @GetMapping("/all-data")
    @PreAuthorize("hasAuthority('role:view_all')")
    public ResponseEntity<?> getAllRolesAndPermissions() {
        return ResponseEntity.ok(new Object[]{
            roleAdminService.getAllRoles(),
            roleAdminService.getAllPermissions()
        });
    }
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:view_permissions')")
    public ResponseEntity<Set<UUID>> getRolePermissions(@PathVariable UUID roleId) {
        return ResponseEntity.ok(roleAdminService.getPermissionIdsForRole(roleId));
    }
    @GetMapping("/{roleId}/details")
    @PreAuthorize("hasAuthority('role:view_all')")
    public ResponseEntity<?> getRoleDetails(@PathVariable UUID roleId) {
        try {
            return ResponseEntity.ok(roleAdminService.getRoleDetails(roleId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('role:create') or hasRole('ADMIN')")
    public ResponseEntity<Role> createRole(@Valid @RequestBody RoleRequest request) {
        try {
            Role newRole = roleAdminService.createRole(request.name(), request.description());
            return new ResponseEntity<>(newRole, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:manage_permissions') or hasRole('ADMIN')")
    public ResponseEntity<Role> updatePermissions(@PathVariable UUID roleId, @RequestBody List<UUID> newPermissionIds) {
        try {
            Role updatedRole = roleAdminService.updateRolePermissions(roleId, newPermissionIds);
            return ResponseEntity.ok(updatedRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:update') or hasRole('ADMIN')")
    public ResponseEntity<Role> updateRole(@PathVariable UUID roleId, @Valid @RequestBody RoleRequest request) {
        try {
            Role updatedRole = roleAdminService.updateRole(roleId, request.name(), request.description());
            return ResponseEntity.ok(updatedRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:delete') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteRole(@PathVariable UUID roleId) {
        try {
            roleAdminService.deleteRole(roleId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}