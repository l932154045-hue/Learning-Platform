package com.learning.learning.client;

import com.learning.common.core.dto.CourseFeignResp;
import com.learning.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "course-service", path = "/api/course")
public interface CourseClient {
    @GetMapping("/detail/{id}")
    R<CourseFeignResp> getCourseDetail(@PathVariable("id") Long id);

    @GetMapping("/batch")
    R<List<CourseFeignResp>> getCourseBatch(@RequestParam("ids") List<Long> ids);
}
