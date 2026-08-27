package cloudmart.product.service;

import cloudmart.product.dto.ProductQueryDTO;
import cloudmart.product.entity.Product;
import cloudmart.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Duration;
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    private final StringRedisTemplate redisTemplate;
    private static final String PRODUCT_CACHE_KEY = "product:detail";
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public boolean updateById(Product entity){
        boolean result= super.updateById(entity);
        if(result){
            //更新后删除缓存
            redisTemplate.delete(PRODUCT_CACHE_KEY + entity.getId());
        }
        return result;
    }
    /**
     * 分页 + 多条件查询
     */
    public IPage<Product> queryPage(ProductQueryDTO dto){
        LambdaQueryWrapper<Product> wrapper=new LambdaQueryWrapper<>();
        // 条件筛选
        if(dto.getCategoryId()!=null){
            wrapper.eq(Product::getCategoryId,dto.getCategoryId());
        }
        if(dto.getKeyword()!=null && !dto.getKeyword().isBlank()){
            wrapper.like(Product::getName,dto.getKeyword());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, dto.getMaxPrice());
        }
        // 默认只查上架商品（前端可传 status=1）
        if (dto.getStatus() != null) {
            wrapper.eq(Product::getStatus, dto.getStatus());
        }

        // 排序
        if ("price".equals(dto.getSortField())) {
            wrapper.orderBy(true, "asc".equals(dto.getSortOrder()),
                    Product::getPrice);
        } else if ("sales".equals(dto.getSortField())) {
            wrapper.orderByDesc(Product::getSales);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt); // 默认最新
        }

        return this.page(new Page<>(dto.getPage(), dto.getPageSize()), wrapper);
    }
    /**
     * 用户端商品详情，只返回上架商品
     */
    public Product detail(Long id) {
        String cacheKey = "product:detail:" + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, Product.class);
            } catch (Exception e) {
                log.warn("商品缓存反序列化失败: {}",id,e);
            }
        }
        Product product = getById(id);
        if (product == null || product.getStatus() == 0) {
            throw new BizException("商品不存在或已下架");
        }

        try {
            long ttl = 30 * 60 + (long) (Math.random() * 600);
            redisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(product),
                    Duration.ofSeconds(ttl));
        } catch (Exception e) {
            log.warn("商品缓存写入失败: {}", id, e);
        }
        return product;
    }
    /**
     * 管理端新增商品
     */
    public void createProduct(Product product) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }
        this.save(product);
    }

    /**
     * 管理端修改商品，不允许通过这个接口直接篡改销量
     */
    public void updateProduct(Product product) {
        product.setSales(null);
        updateById(product);
        redisTemplate.delete("product:detail:" + product.getId());
    }

    /**
     * 管理端删除商品，MyBatis-Plus 逻辑删除
     */
    public void deleteProduct(Long id) {
        this.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long id,Integer quantity){
        Product p = this.getById(id);
        if(p==null || p.getStatus()==0){
            throw new BizException("商品不存在或已下架");
        }
        if(p.getStock()<quantity){
            throw new BizException("库存不足");
        }
        p.setStock(p.getStock() - quantity);
        p.setSales(p.getSales() + quantity);
        this.updateById(p);
        redisTemplate.delete("product:detail:" + id); // 清缓存
    }

    private static final String DEDUCT_FLAG_PREFIX = "stock:deducted:";

    /** 幂等扣库存：以 orderNo 去重，重复消费直接成功返回 */
    public void deductStockIdempotent(String orderNo, Long productId, Integer quantity) {
        String flagKey = DEDUCT_FLAG_PREFIX + orderNo + ":" + productId;
        Boolean first = redisTemplate.opsForValue().setIfAbsent(flagKey, "1", java.time.Duration.ofDays(1));
        if (Boolean.FALSE.equals(first)) {
            return;
        }
        deductStock(productId, quantity);
    }

    /** 恢复库存（取消订单时） */
    public void restoreStock(Long id, Integer quantity) {
        Product p = this.getById(id);
        if (p != null) {
            p.setStock(p.getStock() + quantity);
            this.updateById(p);
            redisTemplate.delete("product:detail:" + id);
        }
    }

}
