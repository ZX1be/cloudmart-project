package cloudmart.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
    //树形结构用这个字段存子节点，数据库不存在
    @TableField(exist = false)
    private List<Category> children;
}
