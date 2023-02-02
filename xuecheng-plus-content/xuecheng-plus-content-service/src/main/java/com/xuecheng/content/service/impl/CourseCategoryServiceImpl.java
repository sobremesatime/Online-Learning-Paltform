package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //得到了根节点下边的所有子节点
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //定义一个list作为最终返回的数据
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = new ArrayList<>();
        HashMap<String, CourseCategoryTreeDto> hashMap = new HashMap<>();

        //将数据封装到List集合中，只包括了根节点的直接下属结点
        categoryTreeDtos.stream().forEach(item->{
            hashMap.put(item.getId(), item);
            if (item.getParentid().equals(id)){
                courseCategoryTreeDtos.add(item);
            }
            //找到父节点
            String parentId = item.getParentid();
            //找到父节点的对象
            CourseCategoryTreeDto parentNode = hashMap.get(parentId);
            if (parentNode != null){
                List<CourseCategoryTreeDto> childrenTreeNodes = parentNode.getChildrenTreeNodes();
                if (childrenTreeNodes == null){
                    parentNode.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //找到子节点，放到父节点中
                parentNode.getChildrenTreeNodes().add(item);
            }
        });
        return courseCategoryTreeDtos;
    }
}
