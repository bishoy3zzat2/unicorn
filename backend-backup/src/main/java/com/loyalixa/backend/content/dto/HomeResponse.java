package com.loyalixa.backend.content.dto;
import com.loyalixa.backend.content.dto.AdvantageFeatureResponse;  
import com.loyalixa.backend.content.dto.FaqResponse;  
import com.loyalixa.backend.content.dto.PartnerResponse;  
import com.loyalixa.backend.course.dto.CourseResponse;
import com.loyalixa.backend.course.dto.ReviewResponse;
import java.util.List;
public record HomeResponse(
    long totalStudents,
    long totalCourses,
    long totalInstructors,
    List<CourseResponse> featuredCourses,
    List<ReviewResponse> featuredReviews,
    List<AdvantageFeatureResponse> advantages, 
    List<FaqResponse> faqs,
    List<PartnerResponse> partners
) {}