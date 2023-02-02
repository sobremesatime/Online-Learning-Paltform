package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CourseCategoryService {
    /**
     * 课程分类树形结构查询
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
