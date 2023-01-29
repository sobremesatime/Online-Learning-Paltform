package com.xuecheng;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XuechengPlusContentServiceApplicationTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(74L);
        Assertions.assertNotNull(courseBase);
    }

    @Test
    public void testCourseBaseInfoService(){
        PageParams pageParams = new PageParams();
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(pageParams, new QueryCourseParamsDto());
        System.out.println(pageResult);
    }

}
