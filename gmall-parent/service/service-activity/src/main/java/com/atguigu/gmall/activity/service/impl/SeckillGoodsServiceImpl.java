package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@SuppressWarnings("all")
public class SeckillGoodsServiceImpl implements SeckillGoodsService {



    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    /**
     * 查询秒杀列表
     * @return
     */
    @Override
    public List<SeckillGoods> findAll() {

        List<SeckillGoods> values = redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS);

//         List<SeckillGoods> values= redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return values;
    }

    /**
     * 获取商品详情
     * @param skuId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        return (SeckillGoods) this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());
    }

    /**
     * 抢单
     * @param userId
     * @param skuId
     */
    @Override
    public void seckillOrder(String userId, Long skuId) {

        //校验状态位
        String stat = (String) CacheHelper.get(skuId.toString());
        if("0".equals(stat)){
            return ;
        }

        //校验是否下过单
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId.toString());

        if(!flag){
            return;
        }


        //校验库存
        String stockSkuId = (String) this.redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
          if(StringUtils.isEmpty(stockSkuId))  {
              this.redisTemplate.convertAndSend("seckillPush",skuId+":0");
              return ;
          }



        //生成订单信息存储到redis
        OrderRecode orderRecode =new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setNum(1);
        SeckillGoods seckillGoods = this.getSeckillGoods(skuId);
        orderRecode.setSeckillGoods(seckillGoods);
        //订单码
        orderRecode.setOrderStr(MD5.encrypt(userId+skuId));

        //存储
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(userId,orderRecode);


        //更新库存量
        this.updateStockCount(skuId);

    }

    /***
     * 根据商品id与用户ID查看订单信息
     * @param skuId
     * @param userId
     * @return
     */
    @Override
    public Result checkOrder(Long skuId, String userId) {
        Boolean isExist = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        if (isExist) {
//            判断用户是否正在排队
//            判断用户是否下单
            Boolean isHasKey = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            if (isHasKey) {
//                抢单成功
                OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
//                秒杀成功
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }

//        判断是否下单
        Boolean isExistOrder = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        if (isExistOrder) {
            String orderId = (String) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state)) {
            return Result.build(null, ResultCodeEnum.SECKILL_FAIL);
        }

//        正在排队中
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    /**
     * 修改mysql 修改 redis
     * @param skuId
     */
    private void updateStockCount(Long skuId) {

        //获取锁对象
        Lock lock=new ReentrantLock();
        //加锁
        lock.lock();
        try {
            //获取库存
            Long stockKey = this.redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();


            //获取当前尚品的信息
            SeckillGoods seckillGoods = this.getSeckillGoods(skuId);
            //设置库存
            seckillGoods.setStockCount(stockKey.intValue());
            //修改数据库mysql
            this.seckillGoodsMapper.updateById(seckillGoods);

            //更新缓存
            this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId.toString(),seckillGoods);
        } finally {
            lock.unlock();
        }


    }
}
