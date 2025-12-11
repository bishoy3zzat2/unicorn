package com.loyalixa.backend.security;
import com.loyalixa.backend.security.dto.PermissionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/permissions")
public class PermissionAdminController {
    private final PermissionAdminService permissionAdminService;
    public PermissionAdminController(PermissionAdminService permissionAdminService) {
        this.permissionAdminService = permissionAdminService;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('permission:view') or hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionAdminService.getAllPermissions());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:view') or hasRole('ADMIN')")
    public ResponseEntity<Permission> getPermission(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(permissionAdminService.getPermissionById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<?> getPermissionDetails(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(permissionAdminService.getPermissionDetails(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('permission:create') or hasRole('ADMIN')")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody PermissionRequest request) {
        try {
            Permission newPermission = permissionAdminService.createPermission(request.name(), request.description());
            return new ResponseEntity<>(newPermission, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update') or hasRole('ADMIN')")
    public ResponseEntity<Permission> updatePermission(@PathVariable UUID id, @Valid @RequestBody PermissionRequest request) {
        try {
            Permission updated = permissionAdminService.updatePermission(id, request.name(), request.description());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete') or hasRole('ADMIN')")
    public ResponseEntity<?> deletePermission(@PathVariable UUID id) {
        try {
            permissionAdminService.deletePermission(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
