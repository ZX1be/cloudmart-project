package common.constant;

public final class MqConst {
    public static final String ORDER_STOCK_QUEUE = "order.stock.queue";
    public static final String ORDER_STOCK_EXCHANGE = "order.stock.exchange";
    public static final String ORDER_STOCK_ROUTING = "order.stock";
    public static final String ORDER_STOCK_DLX = "order.stock.dlx";
    public static final String ORDER_STOCK_DLQ = "order.stock.dlq";
    public static final String ORDER_STOCK_DLQ_ROUTING = "order.stock.dlq";

    public static final String STOCK_RESULT_QUEUE = "stock.result.queue";
    public static final String STOCK_RESULT_EXCHANGE = "stock.result.exchange";
    public static final String STOCK_RESULT_ROUTING = "stock.result";
    public static final String STOCK_RESULT_DLX = "stock.result.dlx";
    public static final String STOCK_RESULT_DLQ = "stock.result.dlq";
    public static final String STOCK_RESULT_DLQ_ROUTING = "stock.result.dlq";

    public static final String SECKILL_DLX = "seckill.order.dlx";
    public static final String SECKILL_DLQ = "seckill.order.dlq";
    public static final String SECKILL_DLQ_ROUTING = "seckill.order.dlq";

    private MqConst() {
    }
}
