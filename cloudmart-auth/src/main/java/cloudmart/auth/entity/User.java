package cloudmart.auth.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private String role;      // USER / ADMIN
    private Integer status;   // 1=正常 0=禁用

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}