package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.SkillRequest;
import com.loyalixa.backend.course.dto.SkillDetailsResponse;
import com.loyalixa.backend.course.dto.SkillResponse;
import com.loyalixa.backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
@Service
public class SkillService {
    private final SkillRepository skillRepository;
    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }
    @Transactional
    public Skill createSkill(SkillRequest request, User adminUser) {
        if (skillRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalStateException("Skill name already exists.");
        }
        Skill newSkill = new Skill();
        newSkill.setName(request.name());
        newSkill.setCreatedBy(adminUser);
        return skillRepository.save(newSkill);
    }
    @Transactional
    public Skill createSkill(SkillRequest request) {
        return createSkill(request, null);
    }
    @Transactional
    public Skill updateSkill(UUID skillId, SkillRequest request, User adminUser) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found."));
        if (skillRepository.existsByNameIgnoreCaseAndIdNot(request.name(), skillId)) {
             throw new IllegalStateException("Another skill with this name already exists.");
        }
        skill.setName(request.name());
        skill.setUpdatedBy(adminUser);
        return skillRepository.save(skill);
    }
    @Transactional
    public Skill updateSkill(UUID skillId, SkillRequest request) {
        return updateSkill(skillId, request, null);
    }
    @Transactional(readOnly = true)
    public SkillDetailsResponse getSkillDetails(UUID skillId) {
        Skill skill = skillRepository.findByIdWithRelations(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found."));
        List<SkillDetailsResponse.CourseInfo> courseInfos = new ArrayList<>();
        if (skill.getCourses() != null && !skill.getCourses().isEmpty()) {
            courseInfos = skill.getCourses().stream()
                    .map(c -> new SkillDetailsResponse.CourseInfo(
                        c.getId(),
                        c.getTitle(),
                        c.getSlug(),
                        c.getStatus()
                    ))
                    .collect(Collectors.toList());
        }
        SkillDetailsResponse.UserInfo createdByInfo = null;
        if (skill.getCreatedBy() != null) {
            User createdBy = skill.getCreatedBy();
            createdByInfo = new SkillDetailsResponse.UserInfo(
                createdBy.getId(),
                createdBy.getEmail(),
                createdBy.getUsername(),
                createdBy.getRole() != null ? createdBy.getRole().getName() : null
            );
        }
        SkillDetailsResponse.UserInfo updatedByInfo = null;
        if (skill.getUpdatedBy() != null) {
            User updatedBy = skill.getUpdatedBy();
            updatedByInfo = new SkillDetailsResponse.UserInfo(
                updatedBy.getId(),
                updatedBy.getEmail(),
                updatedBy.getUsername(),
                updatedBy.getRole() != null ? updatedBy.getRole().getName() : null
            );
        }
        return new SkillDetailsResponse(
            skill.getId(),
            skill.getName(),
            courseInfos,
            createdByInfo,
            updatedByInfo,
            skill.getCreatedAt(),
            skill.getUpdatedAt()
        );
    }
    @Transactional
    public void deleteSkill(UUID skillId) {
        Skill skill = skillRepository.findByIdWithRelations(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found."));
        if (skill.getCourses() != null && !skill.getCourses().isEmpty()) {
            int courseCount = skill.getCourses().size();
            throw new IllegalStateException(
                "Cannot delete skill. It is currently associated with " + courseCount + 
                " course(s). Please remove the skill from all courses before deleting."
            );
        }
        skillRepository.delete(skill);
    }
    @Transactional(readOnly = true)
    public Skill getSkillById(UUID skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found."));
    }
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkillsResponse() {
        List<Skill> skills = skillRepository.findAllWithCreatedAndUpdatedBy();
        return skills.stream()
                .map(this::mapToSkillResponse)
                .collect(Collectors.toList());
    }
    public SkillResponse mapToSkillResponse(Skill skill) {
        SkillResponse.UserInfo createdByInfo = null;
        if (skill.getCreatedBy() != null) {
            createdByInfo = new SkillResponse.UserInfo(
                skill.getCreatedBy().getId(),
                skill.getCreatedBy().getEmail(),
                skill.getCreatedBy().getUsername()
            );
        }
        SkillResponse.UserInfo updatedByInfo = null;
        if (skill.getUpdatedBy() != null) {
            updatedByInfo = new SkillResponse.UserInfo(
                skill.getUpdatedBy().getId(),
                skill.getUpdatedBy().getEmail(),
                skill.getUpdatedBy().getUsername()
            );
        }
        return new SkillResponse(
            skill.getId(),
            skill.getName(),
            skill.getCreatedAt(),
            skill.getUpdatedAt(),
            createdByInfo,
            updatedByInfo
        );
    }
}