package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.PartnerRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/partners")
public class PartnerAdminController {
    private final PartnerService partnerService;
    private final PartnerRepository partnerRepository;
    public PartnerAdminController(PartnerService partnerService, PartnerRepository partnerRepository) {
        this.partnerService = partnerService;
        this.partnerRepository = partnerRepository;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('partner:view') or hasRole('ADMIN') ")
    public ResponseEntity<List<Partner>> getAllPartners() {
        return ResponseEntity.ok(partnerRepository.findAllByOrderByOrderIndexAsc());
    }
    @PostMapping
    @PreAuthorize("hasAuthority('partner:create') or hasRole('ADMIN')")
    public ResponseEntity<Partner> createPartner(@Valid @RequestBody PartnerRequest request) {
        Partner newPartner = partnerService.createPartner(request);
        return new ResponseEntity<>(newPartner, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('partner:update') or hasRole('ADMIN')")
    public ResponseEntity<Partner> updatePartner(@PathVariable Long id, @Valid @RequestBody PartnerRequest request) {
        Partner updatedPartner = partnerService.updatePartner(id, request);
        return ResponseEntity.ok(updatedPartner);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('partner:delete') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        partnerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}