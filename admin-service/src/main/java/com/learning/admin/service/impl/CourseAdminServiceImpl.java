package com.learning.admin.service.impl;

import com.learning.admin.dto.req.CourseSaveReq;
import com.learning.admin.dto.req.VideoSaveReq;
import com.learning.admin.mq.message.CourseUpdatedMessage;
import com.learning.admin.mq.producer.CourseEventProducer;
import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.CourseAdminService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAdminServiceImpl implements CourseAdminService {

    private final AdminAuthService authService;
    private final CourseEventProducer courseEventProducer;

    @Override
    public void createCourse(CourseSaveReq req, Integer role) {
        authService.checkAdmin(role);
        // Feign调用course-service创建课程（此处先用MQ+日志占位）
        log.info("管理员创建课程: title={}", req.getTitle());
    }

    @Override
    public void updateCourse(Long id, CourseSaveReq req, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员更新课程: id={}", id);
        CourseUpdatedMessage msg = new CourseUpdatedMessage();
        msg.setCourseId(id);
        msg.setOperation(1);
        courseEventProducer.sendCourseUpdated(msg);
    }

    @Override
    public void deleteCourse(Long id, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员删除课程: id={}", id);
        CourseUpdatedMessage msg = new CourseUpdatedMessage();
        msg.setCourseId(id);
        msg.setOperation(3);
        courseEventProducer.sendCourseUpdated(msg);
    }

    @Override
    public void updateStatus(Long id, Integer status, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员更新课程状态: id={}, status={}", id, status);
        CourseUpdatedMessage msg = new CourseUpdatedMessage();
        msg.setCourseId(id);
        msg.setOperation(2);
        courseEventProducer.sendCourseUpdated(msg);
    }

    @Override
    public void addVideo(VideoSaveReq req, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员添加视频: courseId={}, video={}", req.getCourseId(), req.getVideoTitle());
    }

    @Override
    public void updateVideo(Long id, VideoSaveReq req, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员更新视频: id={}", id);
    }

    @Override
    public void deleteVideo(Long id, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员删除视频: id={}", id);
    }
}
