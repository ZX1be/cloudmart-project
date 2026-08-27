package cloudmart.order.service;

import cloudmart.order.client.ProductClient;
import cloudmart.order.entity.CartItem;
import cloudmart.order.mapper.CartItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.dto.ProductDTO;
import common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService extends ServiceImpl<CartItemMapper, CartItem> {
    private final ProductClient productClient;

    public List<CartItem> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreatedAt));
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BizException("购买数量必须大于0");
        }

        ProductDTO product = productClient.getById(productId).getData();

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
    public void updateQuantity(Long userId, Long id, Integer quantity) {
        CartItem item = getOwnedItem(userId, id);
        if (quantity == null || quantity <= 0) {
            throw new BizException("购买数量必须大于0");
        }
        item.setQuantity(quantity);
        this.updateById(item);
    }

    public void updateSelected(Long userId, Long id, Integer selected) {
        CartItem item = getOwnedItem(userId, id);
        item.setSelected(selected);
        this.updateById(item);
    }

    public void deleteItem(Long userId, Long id) {
        getOwnedItem(userId, id);
        this.removeById(id);
    }

    private CartItem getOwnedItem(Long userId, Long id) {
        CartItem item = this.getById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BizException("购物车项不存在");
        }
        return item;
    }


}
