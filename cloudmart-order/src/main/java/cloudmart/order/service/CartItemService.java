package cloudmart.order.service;

import cloudmart.order.entity.CartItem;
import cloudmart.order.mapper.CartItemMapper;
import cloudmart.product.entity.Product;
import cloudmart.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService extends ServiceImpl<CartItemMapper, CartItem> {
    private final ProductMapper productMapper;

    public List<CartItem> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreatedAt));
    }

    @Transactional
    public void add(Long userId, Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BizException("商品不存在或已下架");
        }
        if (product.getStock() < quantity) {
            throw new BizException("库存不足");
        }
        // 已存在则累加数量
        CartItem exist = this.getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + quantity);
            this.updateById(exist);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setSelected(1);
            this.save(item);
        }
    }


}
