package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.activity.client.impl.ActivityDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(value = "service-activity" ,fallback = ActivityDegradeFeignClient.class)
public interface ActivityFeignClient {

    /**
     * api/activity/seckill/findAll
     * 查询秒杀列表
     * @return
     */
    @GetMapping("api/activity/seckill/findAll")
    public Result findAll();

    /**
     *  /api/activity/seckill/getSeckillGoods/{skuId}
     * 获取秒杀商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId);

    /**
     * 秒杀确认订单
     * @return
     */
    @GetMapping("/api/activity/seckill/auth/trade")
    Result<Map<String, Object>> trade();

}
