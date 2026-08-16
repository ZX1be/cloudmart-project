package cloudmart.product.service;

import cloudmart.product.dto.ReviewDTO;
import cloudmart.product.entity.Review;
import cloudmart.product.mapper.ReviewMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {

    private final ProductService productService;
    public void addReview(Long userId, Long productId, ReviewDTO dto){
        productService.detail(productId);
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        this.save(review);
    }
    public IPage<Review> listByProductId(Long productId, Integer page, Integer size) {
        return this.page(new Page<>(page, size),
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, productId)
                        .orderByDesc(Review::getCreatedAt));
    }

}
