package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    /**
     * 查询秒杀列表
     * @return
     */
    List<SeckillGoods> findAll();

    /**
     * 商品详情
     * @param skuId
     * @return
     */
    SeckillGoods getSeckillGoods(Long skuId);

    /**
     * 抢单
     * @param userId
     * @param skuId
     */
    void seckillOrder(String userId, Long skuId);

    /***
     * 根据商品id与用户ID查看订单信息
     * @param skuId
     * @param userId
     * @return
     */
    Result checkOrder(Long skuId, String userId);

}
