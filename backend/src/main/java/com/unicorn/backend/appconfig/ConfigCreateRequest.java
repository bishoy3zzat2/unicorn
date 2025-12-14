package com.unicorn.backend.appconfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a config entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCreateRequest {
    private String key;
    private String value;
    private String description;
    private String category;
    private String valueType;
}
