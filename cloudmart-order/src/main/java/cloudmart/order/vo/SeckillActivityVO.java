package cloudmart.order.vo;

import cloudmart.order.entity.SeckillActivity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillActivityVO extends SeckillActivity {
    /** Redis 实时剩余库存 */
    private Integer realStock;
}
