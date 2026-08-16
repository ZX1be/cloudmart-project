package cloudmart.product.controller;

import cloudmart.product.dto.ReviewDTO;
import cloudmart.product.entity.Review;
import cloudmart.product.service.ReviewService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    //添加评价
    @PostMapping("/{productId}/reviews")
    public Result<Void> addReview(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable Long productId,
                                  @Valid @RequestBody ReviewDTO dto){
        reviewService.addReview(userId,productId,dto);
        return Result.success();
    }

    //评价列表（分页）
    @GetMapping("/{productId}/reviews")
    public Result<IPage<Review>> listReviews(@PathVariable Long productId,@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10")Integer size){
        return Result.success(reviewService.listByProductId(productId,page,size));
    }
}
