package com.loyalixa.backend.security;
import com.loyalixa.backend.security.dto.PermissionDetailsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class PermissionAdminService {
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final com.loyalixa.backend.user.UserRepository userRepository;
    private final RoleRepository roleRepository;
    public PermissionAdminService(PermissionRepository permissionRepository, RolePermissionRepository rolePermissionRepository, com.loyalixa.backend.user.UserRepository userRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Permission getPermissionById(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found."));
    }
    @Transactional
    public Permission createPermission(String name, String description) {
        if (permissionRepository.existsByName(name)) {
            throw new IllegalStateException("Permission with name '" + name + "' already exists.");
        }
        Permission newPermission = new Permission();
        newPermission.setName(name);
        newPermission.setDescription(description);
        Permission savedPermission = permissionRepository.save(newPermission);
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole != null) {
            boolean alreadyLinked = rolePermissionRepository.existsByRoleIdAndPermissionId(
                    adminRole.getId(), savedPermission.getId());
            if (!alreadyLinked) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(adminRole);
                rolePermission.setPermission(savedPermission);
                rolePermissionRepository.save(rolePermission);
            }
        }
        return savedPermission;
    }
    @Transactional
    public Permission updatePermission(UUID id, String name, String description) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found."));
        if (!permission.getName().equals(name)) {
            if (permissionRepository.existsByName(name)) {
                throw new IllegalStateException("Permission with name '" + name + "' already exists.");
            }
        }
        permission.setName(name);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }
    @Transactional
    public void deletePermission(UUID id) {
        if (!permissionRepository.existsById(id)) {
            throw new IllegalArgumentException("Permission not found.");
        }
        long roleCount = rolePermissionRepository.countByPermissionId(id);
        if (roleCount > 0) {
            throw new IllegalStateException("Cannot delete permission. There are " + roleCount + " role(s) using this permission. Please remove this permission from these roles first.");
        }
        permissionRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public PermissionDetailsResponse getPermissionDetails(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found."));
        List<Role> rolesWithPermission = roleRepository.findAll().stream()
                .filter(role -> role.getPermissions() != null && 
                        role.getPermissions().stream()
                                .anyMatch(p -> p.getId().equals(permissionId)))
                .collect(Collectors.toList());
        List<PermissionDetailsResponse.RoleInfo> rolesInfo = rolesWithPermission.stream()
                .map(role -> {
                    long usersCount = userRepository.countByRoleId(role.getId());
                    return new PermissionDetailsResponse.RoleInfo(
                            role.getId(),
                            role.getName(),
                            role.getDescription(),
                            usersCount
                    );
                })
                .collect(Collectors.toList());
        return new PermissionDetailsResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                rolesInfo.size(),
                rolesInfo
        );
    }
}
