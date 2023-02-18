package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

@Component
public class OrderDegradeFeignClient implements OrderFeignClient {
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }

    @Override
    public Result trade() {
        return Result.fail();
    }

    @Override
    public Long submitOrder(OrderInfo orderInfo) {
        return null;
    }
}
