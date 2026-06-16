package com.learning.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Select("<script>SELECT * FROM course WHERE status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='categoryIds != null and categoryIds.size() > 0'>" +
            "AND category_id IN <foreach collection='categoryIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>" +
            "</if> " +
            "AND price BETWEEN #{priceMin} AND #{priceMax} " +
            "<choose>" +
            "<when test='sort == \"price_asc\"'>ORDER BY price ASC</when>" +
            "<when test='sort == \"price_desc\"'>ORDER BY price DESC</when>" +
            "<when test='sort == \"newest\"'>ORDER BY created_at DESC</when>" +
            "<otherwise>ORDER BY sale_count DESC</otherwise>" +
            "</choose></script>")
    IPage<Course> searchCourses(Page<Course> page, @Param("keyword") String keyword,
            @Param("categoryIds") List<Long> categoryIds, @Param("priceMin") BigDecimal priceMin,
            @Param("priceMax") BigDecimal priceMax, @Param("sort") String sort);
}
