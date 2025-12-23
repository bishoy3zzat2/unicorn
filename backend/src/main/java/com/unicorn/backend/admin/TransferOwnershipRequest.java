package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for ownership transfer request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferOwnershipRequest {
    private UUID newOwnerId;
    private String newOwnerRole; // Optional role for the new owner (e.g. FOUNDER, CEO, etc.)
}
