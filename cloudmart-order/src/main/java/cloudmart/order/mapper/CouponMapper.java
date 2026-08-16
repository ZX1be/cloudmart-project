package cloudmart.order.mapper;

import cloudmart.order.entity.Coupon;
import cloudmart.order.entity.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CouponMapper  extends BaseMapper<Coupon> {
}