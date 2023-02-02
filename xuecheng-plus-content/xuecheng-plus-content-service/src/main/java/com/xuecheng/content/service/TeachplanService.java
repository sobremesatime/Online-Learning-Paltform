package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @Title: TeachplanService
 * @Description: 课程基本信息管理业务接口
 * @author: LYL
 * @date: 2023/1/30 19:13
 */
public interface TeachplanService {
    /**
     * @description 查询课程计划树型结构
     * @param courseId  课程id
     * @return List<TeachplanDto>
     */
    public List<TeachplanDto> findTeachplayTree(Long courseId);

    /**
     * @description 保存课程计划(新增/修改)
     * @param dto
     * @return void
     * @author Mr.M
     * @date 2022/10/10 15:07
     */
    public void saveTeachplan(SaveTeachplanDto dto);
}
