package cloudmart.product.mapper;

import cloudmart.product.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    @Insert("""
        INSERT IGNORE INTO stock_deduct_record(order_no, product_id, quantity, status)
        VALUES(#{orderNo}, #{productId}, #{quantity}, 1)
        """)
    int insertIgnoreDeductRecord(@Param("orderNo") String orderNo,
                                 @Param("productId") Long productId,
                                 @Param("quantity") Integer quantity);

    @Update("""
        UPDATE product
        SET stock = stock - #{quantity},
            sales = sales + #{quantity}
        WHERE id = #{productId}
          AND status = 1
          AND deleted = 0
          AND stock >= #{quantity}
        """)
    int deductStockAtomically(@Param("productId") Long productId,
                              @Param("quantity") Integer quantity);


    @Insert("""
    INSERT IGNORE INTO stock_restore_record(
        order_no, product_id, quantity, status
    )
    VALUES(#{orderNo}, #{productId}, #{quantity}, 1)
    """)
    int insertIgnoreRestoreRecord(@Param("orderNo") Long orderNo,
                                  @Param("product_id")Long productId,
                                  @Param("quantity") Integer quantity);


    @Update("""
    UPDATE product
    SET stock = stock + #{quantity},
        sales = GREATEST(sales - #{quantity}, 0)
    WHERE id = #{productId}
      AND deleted = 0
    """)
    int restoreStockAtomically(@Param("product_id")Long productId,
                               @Param("quantity") Integer quantity);
}
