package cloudmart.order.client;

import common.dto.ProductDTO;
import common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cloudmart-product")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    Result<ProductDTO> getById(@PathVariable("id") Long id);
    @PostMapping("/api/products/{id}/deduct")
    Result<Void> deductStock(@PathVariable("id") Long id,
                             @RequestParam("orderNo") String orderNo,
                             @RequestParam("quantity") Integer quantity);

    @PostMapping("/api/products/{id}/restore")
    Result<Void> restoreStock(@PathVariable("id") Long id,
                              @RequestParam("quantity") Integer quantity);
    }

