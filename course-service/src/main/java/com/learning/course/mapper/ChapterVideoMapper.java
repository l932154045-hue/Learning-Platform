package com.learning.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.course.entity.ChapterVideo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChapterVideoMapper extends BaseMapper<ChapterVideo> {
    @Select("SELECT * FROM chapter_video WHERE course_id = #{courseId} ORDER BY sort_order ASC")
    List<ChapterVideo> selectByCourseId(@Param("courseId") Long courseId);

    @Delete("DELETE FROM chapter_video WHERE course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);
}
