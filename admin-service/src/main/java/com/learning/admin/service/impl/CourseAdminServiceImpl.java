package com.learning.admin.service.impl;

import com.learning.admin.client.CourseServiceClient;
import com.learning.admin.dto.req.CourseSaveReq;
import com.learning.admin.dto.req.VideoSaveReq;
import com.learning.admin.mq.message.CourseUpdatedMessage;
import com.learning.admin.mq.producer.AdminEventProducer;
import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.CourseAdminService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAdminServiceImpl implements CourseAdminService {

    private final AdminAuthService authService;
    private final AdminEventProducer eventProducer;
    private final CourseServiceClient courseServiceClient;

    @Override
    public void createCourse(CourseSaveReq req, Integer role) {
        authService.checkAdmin(role);
        R<Long> result = courseServiceClient.createCourse(req);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        log.info("管理员创建课程成功: title={}, id={}", req.getTitle(), result.getData());
    }

    @Override
    public void updateCourse(Long id, CourseSaveReq req, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.updateCourse(id, req);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        // Send MQ for cache refresh
        CourseUpdatedMessage msg = new CourseUpdatedMessage();
        msg.setCourseId(id);
        msg.setOperation(1);
        eventProducer.sendCourseUpdated(msg);
        log.info("管理员更新课程成功: id={}", id);
    }

    @Override
    public void deleteCourse(Long id, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.deleteCourse(id);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        CourseUpdatedMessage msg = new CourseUpdatedMessage();
        msg.setCourseId(id);
        msg.setOperation(3);
        eventProducer.sendCourseUpdated(msg);
        log.info("管理员删除课程成功: id={}", id);
    }

    @Override
    public void updateStatus(Long id, Integer status, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.updateStatus(id, status);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        CourseUpdatedMessage msg = new CourseUpdatedMessage();
        msg.setCourseId(id);
        msg.setOperation(2);
        eventProducer.sendCourseUpdated(msg);
        log.info("管理员更新课程状态: id={}, status={}", id, status);
    }

    @Override
    public void addVideo(VideoSaveReq req, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.addVideo(req);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        log.info("管理员添加视频: courseId={}, video={}", req.getCourseId(), req.getVideoTitle());
    }

    @Override
    public void updateVideo(Long id, VideoSaveReq req, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.updateVideo(id, req);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        log.info("管理员更新视频: id={}", id);
    }

    @Override
    public void deleteVideo(Long id, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.deleteVideo(id);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        log.info("管理员删除视频: id={}", id);
    }
}
