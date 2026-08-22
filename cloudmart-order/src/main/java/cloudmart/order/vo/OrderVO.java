package cloudmart.order.vo;

import cloudmart.order.entity.Order;
import cloudmart.order.entity.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Order {
    private List<OrderItem> items;
}