package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.TagRequest;
import com.loyalixa.backend.course.dto.TagDetailsResponse;
import com.loyalixa.backend.course.dto.TagResponse;
import com.loyalixa.backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
@Service
public class TagService {
    private final TagRepository tagRepository;
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    @Transactional
    public Tag createTag(TagRequest request, User adminUser) {
        if (tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalStateException("Tag name already exists.");
        }
        Tag newTag = new Tag();
        newTag.setName(request.name());
        newTag.setCreatedBy(adminUser);
        return tagRepository.save(newTag);
    }
    @Transactional
    public Tag createTag(TagRequest request) {
        return createTag(request, null);
    }
    @Transactional
    public Tag updateTag(UUID tagId, TagRequest request, User adminUser) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found."));
        if (tagRepository.existsByNameIgnoreCaseAndIdNot(request.name(), tagId)) {
             throw new IllegalStateException("Another tag with this name already exists.");
        }
        tag.setName(request.name());
        tag.setUpdatedBy(adminUser);
        return tagRepository.save(tag);
    }
    @Transactional
    public Tag updateTag(UUID tagId, TagRequest request) {
        return updateTag(tagId, request, null);
    }
    @Transactional(readOnly = true)
    public TagDetailsResponse getTagDetails(UUID tagId) {
        Tag tag = tagRepository.findByIdWithRelations(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found."));
        List<TagDetailsResponse.CourseInfo> courseInfos = new ArrayList<>();
        if (tag.getCourses() != null && !tag.getCourses().isEmpty()) {
            courseInfos = tag.getCourses().stream()
                    .map(c -> new TagDetailsResponse.CourseInfo(
                        c.getId(),
                        c.getTitle(),
                        c.getSlug(),
                        c.getStatus()
                    ))
                    .collect(Collectors.toList());
        }
        TagDetailsResponse.UserInfo createdByInfo = null;
        if (tag.getCreatedBy() != null) {
            User createdBy = tag.getCreatedBy();
            createdByInfo = new TagDetailsResponse.UserInfo(
                createdBy.getId(),
                createdBy.getEmail(),
                createdBy.getUsername(),
                createdBy.getRole() != null ? createdBy.getRole().getName() : null
            );
        }
        TagDetailsResponse.UserInfo updatedByInfo = null;
        if (tag.getUpdatedBy() != null) {
            User updatedBy = tag.getUpdatedBy();
            updatedByInfo = new TagDetailsResponse.UserInfo(
                updatedBy.getId(),
                updatedBy.getEmail(),
                updatedBy.getUsername(),
                updatedBy.getRole() != null ? updatedBy.getRole().getName() : null
            );
        }
        return new TagDetailsResponse(
            tag.getId(),
            tag.getName(),
            courseInfos,
            createdByInfo,
            updatedByInfo,
            tag.getCreatedAt(),
            tag.getUpdatedAt()
        );
    }
    @Transactional
    public void deleteTag(UUID tagId) {
        Tag tag = tagRepository.findByIdWithRelations(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found."));
        if (tag.getCourses() != null && !tag.getCourses().isEmpty()) {
            int courseCount = tag.getCourses().size();
            throw new IllegalStateException(
                "Cannot delete tag. It is currently associated with " + courseCount + 
                " course(s). Please remove the tag from all courses before deleting."
            );
        }
        tagRepository.delete(tag);
    }
    @Transactional(readOnly = true)
    public Tag getTagById(UUID tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found."));
    }
    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTagsResponse() {
        List<Tag> tags = tagRepository.findAllWithCreatedAndUpdatedBy();
        return tags.stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
    }
    public TagResponse mapToTagResponse(Tag tag) {
        TagResponse.UserInfo createdByInfo = null;
        if (tag.getCreatedBy() != null) {
            createdByInfo = new TagResponse.UserInfo(
                tag.getCreatedBy().getId(),
                tag.getCreatedBy().getEmail(),
                tag.getCreatedBy().getUsername()
            );
        }
        TagResponse.UserInfo updatedByInfo = null;
        if (tag.getUpdatedBy() != null) {
            updatedByInfo = new TagResponse.UserInfo(
                tag.getUpdatedBy().getId(),
                tag.getUpdatedBy().getEmail(),
                tag.getUpdatedBy().getUsername()
            );
        }
        return new TagResponse(
            tag.getId(),
            tag.getName(),
            tag.getCreatedAt(),
            tag.getUpdatedAt(),
            createdByInfo,
            updatedByInfo
        );
    }
}