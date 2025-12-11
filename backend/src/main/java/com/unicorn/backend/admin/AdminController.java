package com.unicorn.backend.admin;

import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import com.unicorn.backend.user.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String query,
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<User> usersPage;
        if (query != null && !query.trim().isEmpty()) {
            usersPage = userRepository.searchUsers(query.trim(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        Page<UserResponse> responsePage = usersPage.map(u -> new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getRole(),
                u.getStatus(),
                u.getCreatedAt(),
                u.getLastLoginAt()));

        return ResponseEntity.ok(responsePage);
    }
}
