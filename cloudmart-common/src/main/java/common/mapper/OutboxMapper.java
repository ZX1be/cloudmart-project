package common.mapper;

import common.entity.OutboxMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OutboxMapper extends BaseMapper<OutboxMessage> {

    @Insert("""
            INSERT IGNORE INTO outbox(
                message_key,
                message_type,
                topic,
                routing_key,
                payload,
                status
            )
            VALUES(
                #{messageKey},
                #{messageType},
                #{topic},
                #{routingKey},
                #{payload},
                0
            )
            """)
    int insertLocal(@Param("messageKey") String messageKey,
                    @Param("messageType") String messageType,
                    @Param("topic") String topic,
                    @Param("routingKey") String routingKey,
                    @Param("payload") String payload);

    @Select("""
            <script>
            SELECT *
            FROM outbox
            WHERE message_type IN
            <foreach collection="messageTypes" item="mt" open="(" separator="," close=")">
                #{mt}
            </foreach>
              AND status IN (0, 3)
              AND (next_retry_at IS NULL OR next_retry_at &lt;= NOW())
            ORDER BY id
            LIMIT #{limit}
            FOR UPDATE SKIP LOCKED
            </script>
            """)
    List<OutboxMessage> selectPendingBatch(@Param("messageTypes") List<String> messageTypes,
                                           @Param("limit") int limit);

    @Update("""
            UPDATE outbox
            SET status = 1
            WHERE id = #{id}
            """)
    int markSent(@Param("id") Long id);

    @Update("""
            UPDATE outbox
            SET status = 3,
                retry_count = retry_count + 1,
                next_retry_at = DATE_ADD(NOW(), INTERVAL 5 SECOND)
            WHERE id = #{id}
            """)
    int markRetry(@Param("id") Long id);
}
