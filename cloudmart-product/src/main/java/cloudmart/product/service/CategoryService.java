package cloudmart.product.service;

import cloudmart.product.entity.Category;
import cloudmart.product.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryService extends ServiceImpl<CategoryMapper,Category> {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    //获取分类树
    public List<Category> getCategoryTree() {
        String cacheKey = "category:tree";
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        new TypeReference<List<Category>>() {});
            } catch (Exception e) {
                log.warn("分类缓存反序列化失败", e);
            }
        }

        List<Category> tree = buildTree();
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(tree),
                    Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("分类缓存写入失败", e);
        }
        return tree;
    }
    private List<Category> getChildren(Long parentId,List<Category> all){
            List<Category> children = all.stream().filter(c->c.getParentId().equals(parentId)).collect(Collectors.toList());
            for(Category child: children) {
                child.setChildren(getChildren(child.getId(),all));
            }
            return children.isEmpty() ? null:children;
    }
    private List<Category> buildTree() {
        List<Category> all = this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        List<Category> roots = all.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
        for (Category root : roots) {
            root.setChildren(getChildren(root.getId(), all));
        }
        return roots;
    }

}


