package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for issuing a warning to a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarnUserRequest {
    private String reason;
}
