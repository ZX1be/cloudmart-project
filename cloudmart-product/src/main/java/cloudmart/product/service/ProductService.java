package cloudmart.product.service;

import cloudmart.product.dto.ProductQueryDTO;
import cloudmart.product.entity.Product;
import cloudmart.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.exception.BizException;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {
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
        Product product = this.getById(id);
        if (product == null || product.getStatus() == 0) {
            throw new BizException("商品不存在或已下架");
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
        this.updateById(product);
    }

    /**
     * 管理端删除商品，MyBatis-Plus 逻辑删除
     */
    public void deleteProduct(Long id) {
        this.removeById(id);
    }
}
