package common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outbox")
public class OutboxMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String messageKey;

    private String messageType;

    private String topic;

    private String routingKey;

    private String payload;

    /**
     * 0待发送 1已发送 3重试 4死信
     */
    private Integer status;

    private Integer retryCount;

    private LocalDateTime nextRetryAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
