package com.unicorn.backend.appconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class AppConfigController {

    private final AppConfigService appConfigService;

    /**
     * Get all publicly available configuration keys.
     * Mobile apps should call this on startup to sync local limits/settings.
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getConfigs() {
        return ResponseEntity.ok(appConfigService.getAllAsMap());
    }
}
