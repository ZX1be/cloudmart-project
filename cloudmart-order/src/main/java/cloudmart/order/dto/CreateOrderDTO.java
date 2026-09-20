package cloudmart.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderDTO {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
    @NotBlank(message = "结算内容指纹不能为空")
    private String cartFingerprint;
    private String addressSnapshot;
    private Long userCouponId;
}