package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import org.springframework.aop.target.LazyInitTargetSource;

import java.util.List;

/**
 * @Title: TeachplanDto
 * @Description: 教学计划与媒资
 * @author: LYL
 * @date: 2023/1/30 15:27
 */
@Data
public class TeachplanDto extends Teachplan {
    //关联媒资信息
    TeachplanMedia teachplanMedia;

    //子目录
    List<TeachplanDto> teachPlanTreeNodes;
}
