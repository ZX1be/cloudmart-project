package cloudmart.auth.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("address")
public class Address {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;

    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}