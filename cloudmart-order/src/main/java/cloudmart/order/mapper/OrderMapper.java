package cloudmart.order.mapper;

import cloudmart.order.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("""
    SELECT * FROM `order`
    WHERE order_no = #{orderNo}
    AND deleted = 0
    FOR UPDATE
    """)
    Order selectByOrderNoForUpdate(@Param("orderNo") String orderNo);
}
