package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

@FeignClient(value = "service-order",fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {


    /**
     *  /api/order/inner/getOrderInfo/{orderId}
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("api/order/inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId);
    /**
     * /api/order/auth/trade
     * 去结算
     * @return
     */
    @GetMapping("/api/order/auth/trade")
    public Result trade();

    /**
     * 提交秒杀订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/api/order/inner/seckill/submitOrder")
    Long submitOrder(@RequestBody OrderInfo orderInfo);

}
