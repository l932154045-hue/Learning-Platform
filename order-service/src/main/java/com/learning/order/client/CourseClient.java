package com.learning.order.client;

import com.learning.common.core.result.R;
import com.learning.common.core.dto.CourseFeignResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service", path = "/api/course")
public interface CourseClient {
    @GetMapping("/detail/{id}")
    R<CourseFeignResp> getCourseDetail(@PathVariable("id") Long id);
}
