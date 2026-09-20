package cloudmart.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_idempotent_record")
public class OrderIdempotentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String requestHash;

    private String idempotentKey;

    private Long orderId;

    private LocalDateTime createdAt;
}