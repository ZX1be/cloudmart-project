package cloudmart.order.service;

import cloudmart.order.entity.Coupon;
import cloudmart.order.entity.UserCoupon;
import cloudmart.order.mapper.CouponMapper;
import cloudmart.order.mapper.UserCouponMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService extends ServiceImpl<CouponMapper, Coupon> {
    private final UserCouponMapper userCouponMapper;
    public List<Coupon> listAvailable(){
        return this.list(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .le(Coupon::getStartTime, LocalDateTime.now())
                .ge(Coupon::getEndTime, LocalDateTime.now()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void claim(Long userId, Long couponId) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null || coupon.getStatus() == 0) {
            throw new BizException("优惠券不存在");
        }
        if (coupon.getUsedCount() >= coupon.getTotalCount()) {
            throw new BizException("优惠券已领完");
        }
        Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            throw new BizException("不能重复领取");
        }
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus("UNUSED");
        userCouponMapper.insert(userCoupon);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        this.updateById(coupon);
    }
    public List<UserCoupon> listMine(Long userId) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreatedAt));
    }


}
