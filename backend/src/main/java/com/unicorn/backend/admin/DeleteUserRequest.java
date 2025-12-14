package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for deleting a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserRequest {
    private String reason;
}
