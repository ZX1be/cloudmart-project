package cloudmart.order.mapper;

import cloudmart.order.entity.OrderIdempotentRecord;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderIdempotentRecordMapper {
    @Insert("""
    INSERT IGNORE INTO order_idempotent_record(
        user_id,
        idempotent_key,
        request_hash,
        order_id
    )
    VALUES(
        #{userId},
        #{idempotentKey},
        #{requestHash},
        NULL
    )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertIgnore(OrderIdempotentRecord record);

    @Select("""
        SELECT id,
               user_id,
               idempotent_key,
               request_hash,
               order_id,
               created_at
        FROM order_idempotent_record
        WHERE user_id = #{userId}
        AND idempotent_key = #{idempotentKey}
        FOR UPDATE
        """)
    OrderIdempotentRecord selectForUpdate(
            @Param("userId") Long userId,
            @Param("idempotentKey") String idempotencyKey);

    @Update("""
        UPDATE order_idempotent_record
        SET order_id = #{orderId}
        WHERE id = #{id}
          AND order_id IS NULL
        """)
    int bindOrderId(
            @Param("id") Long id,
            @Param("orderId") Long orderId);
}
