package com.learning.learning.service;

import com.learning.learning.dto.req.ProgressReportReq;
import com.learning.learning.dto.resp.ProgressVO;

import java.util.List;

public interface ProgressService {
    void report(Long userId, ProgressReportReq req);
    List<ProgressVO> getProgress(Long userId, Long courseId);
}
