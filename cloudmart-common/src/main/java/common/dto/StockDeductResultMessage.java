package common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductResultMessage implements Serializable {
    private String orderNo;
    private Long productId;
    private Integer quantity;
}
