package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.PartnerRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class PartnerService {
    private final PartnerRepository partnerRepository;
    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }
    @Transactional
    public Partner createPartner(PartnerRequest request) {
        Partner partner = new Partner();
        partner.setName(request.name());
        partner.setLogoUrl(request.logoUrl());
        partner.setWebsiteUrl(request.websiteUrl());
        partner.setOrderIndex(request.orderIndex());
        return partnerRepository.save(partner);
    }
    @Transactional
    public Partner updatePartner(Long id, PartnerRequest request) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found."));
        partner.setName(request.name());
        partner.setLogoUrl(request.logoUrl());
        partner.setWebsiteUrl(request.websiteUrl());
        partner.setOrderIndex(request.orderIndex());
        return partnerRepository.save(partner);
    }
}