package com.learning.learning.controller;

import com.learning.common.core.result.R;
import com.learning.learning.dto.req.ProgressReportReq;
import com.learning.learning.dto.resp.MyCourseVO;
import com.learning.learning.dto.resp.ProgressVO;
import com.learning.learning.service.EnrollmentService;
import com.learning.learning.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;

    @GetMapping("/my-courses")
    public R<List<MyCourseVO>> myCourses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(enrollmentService.myCourses(userId));
    }

    @GetMapping("/progress/{courseId}")
    public R<List<ProgressVO>> getProgress(@PathVariable Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(progressService.getProgress(userId, courseId));
    }

    @PutMapping("/progress/report")
    public R<Void> reportProgress(@Valid @RequestBody ProgressReportReq req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        progressService.report(userId, req);
        return R.ok();
    }
}
