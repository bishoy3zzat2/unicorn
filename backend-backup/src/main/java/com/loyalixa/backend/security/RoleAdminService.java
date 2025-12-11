package com.loyalixa.backend.security;
import com.loyalixa.backend.security.dto.RoleDetailsResponse;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.user.dto.UserAdminResponse;
import com.loyalixa.backend.subscription.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class RoleAdminService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    public RoleAdminService(RoleRepository roleRepository, PermissionRepository permissionRepository, RolePermissionRepository rolePermissionRepository, UserRepository userRepository, UserSubscriptionRepository userSubscriptionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Set<String> getPermissionsForRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        return role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
    @Transactional(readOnly = true)
    public Set<UUID> getPermissionIdsForRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        return role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
    }
    @Transactional
    public Role createRole(String name, String description) {
        Optional<Role> existing = roleRepository.findByName(name);
        if (existing.isPresent()) {
            throw new IllegalStateException("Role with name '" + name + "' already exists.");
        }
        Role newRole = new Role();
        newRole.setName(name);
        newRole.setDescription(description);
        return roleRepository.save(newRole);
    }
    @Transactional
    public Role updateRole(UUID roleId, String name, String description) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        if (name != null && !name.equals(role.getName())) {
            Optional<Role> existing = roleRepository.findByName(name);
            if (existing.isPresent() && !existing.get().getId().equals(roleId)) {
                throw new IllegalStateException("Role with name '" + name + "' already exists.");
            }
            role.setName(name);
        }
        if (description != null) {
            role.setDescription(description);
        }
        return roleRepository.save(role);
    }
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        long userCount = userRepository.countByRoleId(roleId);
        if (userCount > 0) {
            throw new IllegalStateException("Cannot delete role. There are " + userCount + " user(s) assigned to this role. Please reassign or remove these users first.");
        }
        rolePermissionRepository.deleteByRoleId(roleId);
        roleRepository.delete(role);
    }
    @Transactional
    public Role updateRolePermissions(UUID roleId, List<UUID> newPermissionIds) {
        Role targetRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        rolePermissionRepository.deleteByRoleId(roleId);
        List<Permission> newPermissions = permissionRepository.findAllById(newPermissionIds);
        Set<UUID> uniquePermissionIds = new HashSet<>(newPermissionIds);
        if (!newPermissions.isEmpty() && newPermissions.size() == uniquePermissionIds.size()) {
            Set<RolePermission> newLinks = newPermissions.stream()
                    .map(permission -> {
                        RolePermission link = new RolePermission();
                        link.setRole(targetRole);
                        link.setPermission(permission);
                        return link;
                    })
                    .collect(Collectors.toSet());
            rolePermissionRepository.saveAll(newLinks);
            rolePermissionRepository.flush();
        }
        roleRepository.flush();
        Role updatedRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found after update."));
        return updatedRole;
    }
    @Transactional(readOnly = true)
    public RoleDetailsResponse getRoleDetails(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));
        List<RoleDetailsResponse.PermissionInfo> permissions = role.getPermissions() != null ?
                role.getPermissions().stream()
                        .map(p -> new RoleDetailsResponse.PermissionInfo(
                                p.getId(),
                                p.getName(),
                                p.getDescription()
                        ))
                        .collect(Collectors.toList()) :
                List.of();
        List<User> users = userRepository.findByRoleId(roleId);
        List<UserAdminResponse> usersResponse = users.stream()
                .map(this::mapToUserAdminResponse)
                .collect(Collectors.toList());
        return new RoleDetailsResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissions.size(),
                permissions,
                users.size(),
                usersResponse
        );
    }
    private UserAdminResponse mapToUserAdminResponse(User user) {
        String actualUsername = user.getActualUsername();
        String currentPlanName = null;
        String currentPlanCode = null;
        Optional<com.loyalixa.backend.subscription.UserSubscription> activeSubscription = 
            userSubscriptionRepository.findActiveSubscriptionByUserId(user.getId(), LocalDateTime.now());
        if (activeSubscription.isPresent()) {
            currentPlanName = activeSubscription.get().getPlan().getName();
            currentPlanCode = activeSubscription.get().getPlan().getCode();
        } else {
            currentPlanName = "Free Plan";
            currentPlanCode = "FREE";
        }
        return new UserAdminResponse(
                user.getId(),
                actualUsername != null ? actualUsername : user.getEmail(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : null,
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getPasswordChangedAt(),
                user.getDeletedAt(),
                user.getDeletionReason(),
                user.getCanAccessDashboard() != null ? user.getCanAccessDashboard() : false,
                currentPlanName,
                currentPlanCode
        );
    }
}