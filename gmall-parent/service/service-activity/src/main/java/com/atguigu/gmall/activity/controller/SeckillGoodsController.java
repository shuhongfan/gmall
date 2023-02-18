package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.service.RabbitService;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsController {
    @Autowired
    private OrderFeignClient orderFeignClient;


    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     *  /api/activity/seckill/auth/seckillOrder/{skuId}
     *  秒杀下单
     *
     * @return
     */
    @PostMapping("/auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId,HttpServletRequest request){

        //校验下单码
        String skuIdStr = request.getParameter("skuIdStr");

        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //校验状态位
        if(MD5.encrypt(userId).equals(skuIdStr)){


            //校验状态  key :skuId 23 value :状态位 1或者0
            String status = (String) CacheHelper.get(skuId.toString());
            //判断
            if(StringUtils.isEmpty(status)){

                return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
            }

            //判断状态值
            if("0".equals(status)){

                return Result.build(null,ResultCodeEnum.SECKILL_FINISH);
            }else{


                //生成抢单信息对象
                UserRecode userRecode=new UserRecode();
                userRecode.setSkuId(skuId);
                userRecode.setUserId(userId);

                //发送消息排队
                this.rabbitService.sendMessage(
                        MqConst.EXCHANGE_DIRECT_SECKILL_USER,
                        MqConst.ROUTING_SECKILL_USER,userRecode
                );

                return Result.ok();

            }



        }



        return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
    }

    /**
     *  /api/activity/seckill/auth/getSeckillSkuIdStr/{skuId}
     * 获取抢单码
     * @return
     */
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request){

        //获取用户id
        String userId = AuthContextHolder.getUserId(request);

        SeckillGoods seckillGoods= (SeckillGoods) this.redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId.toString());

        //判断
        if(seckillGoods!=null){
            //获取当前时间
            Date curDate=new Date();
            //判断
            if(DateUtil.dateCompare(seckillGoods.getStartTime(),curDate)&&DateUtil.dateCompare(curDate,seckillGoods.getEndTime())){

                //生成抢单码
                String encrypt = MD5.encrypt(userId);

                return Result.ok(encrypt);


            }



        }



        return Result.fail().message("获取抢单码失败！");
    }


    /**
     * api/activity/seckill/findAll
     * 查询秒杀列表
     *
     * @return
     */
    @GetMapping("/findAll")
    public Result findAll() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.findAll();

        return Result.ok(seckillGoodsList);
    }


    /**
     *  /api/activity/seckill/getSeckillGoods/{skuId}
     * 获取秒杀商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId) {
      SeckillGoods seckillGoods=  seckillGoodsService.getSeckillGoods(skuId);

        return Result.ok(seckillGoods);
    }

    /***
     * 根据商品id与用户ID查看订单信息
     * @param skuId
     * @return
     */
    @GetMapping(value = "auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        //当前登录用户
        String userId = AuthContextHolder.getUserId(request);
        return seckillGoodsService.checkOrder(skuId, userId);
    }

    /**
     * 秒杀确认订单
     *
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request) {
//        获取到用户ID
        String userId = AuthContextHolder.getUserId(request);

//        先得到用户想要购买的商品
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        if (orderRecode == null) {
            return Result.fail().message("非法操作");
        }
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();

//        获取用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.valueOf(userId));

//        声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());

//        添加到集合
        detailArrayList.add(orderDetail);

    // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", userAddressList);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalAmount", orderInfo.getTotalAmount());
        return Result.ok(result);
    }

    /**
     * 提交秒杀订单
     * @param orderInfo
     * @param request
     * @return
     */
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);


        orderInfo.setUserId(Long.parseLong(userId));

        Long orderId = orderFeignClient.submitOrder(orderInfo);
        if (null == orderId) {
            return Result.fail().message("下单失败，请重新操作");
        }
        //删除下单信息
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);
        //下单记录
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());

        return Result.ok(orderId);
    }

}
