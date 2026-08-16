package cloudmart.product.service;

import cloudmart.product.entity.Category;
import cloudmart.product.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper,Category> {
    //获取分类树
    public List<Category> getCategoryTree(){
        //1.获取启用分类
        List<Category> all = this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));

        //2.找出一级分类,parentId=0
        List<Category> roots=all.stream().filter(c->c.getParentId()==0).collect(Collectors.toList());
        //3.递归填充
        for(Category root:roots){
            root.setChildren(getChildren(root.getId(),all));
        }
        return roots;
    }
    private List<Category> getChildren(Long parentId,List<Category> all){
            List<Category> children = all.stream().filter(c->c.getParentId().equals(parentId)).collect(Collectors.toList());
            for(Category child: children) {
                child.setChildren(getChildren(child.getId(),all));
            }
            return children.isEmpty() ? null:children;
    }

}


