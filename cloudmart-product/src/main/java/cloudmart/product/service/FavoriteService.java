package cloudmart.product.service;

import cloudmart.product.entity.Favorite;
import cloudmart.product.mapper.FavoriteMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> {

    public void add(Long userId, Long productId) {
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        this.save(favorite);
    }

    public void remove(Long userId, Long productId) {
        this.remove(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
    }

    public List<Favorite> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt));
    }
}