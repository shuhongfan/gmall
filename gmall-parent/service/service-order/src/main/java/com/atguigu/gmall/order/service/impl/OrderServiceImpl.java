package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Ordering;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper,OrderInfo> implements OrderService {


    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RabbitService rabbitService;

    //http:/localhost:9001
    @Value("${ware.url}")
    private String wareUrl;
    /**
     * 提交订单
     *
     * 涉及到的表：
     * order_info 订单详情表  --订单的说明
     * order_detail 订单明细表  --商品的说明
     *
     * 一个订单多个订单明细
     * 关联关系:orderDetailList
     *
     * @param orderInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(OrderInfo orderInfo) {

        //总金额
        orderInfo.sumTotalAmount();
        //订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //付款方式
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        //订单交易号
        String outTradeNo="atguigu"+ UUID.randomUUID().toString().replaceAll("-","");
        orderInfo.setOutTradeNo(outTradeNo);

        StringBuilder tradeBody=new StringBuilder();
        //订单描述
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            tradeBody.append(orderDetail.getSkuName()+"   ");

        }
        //设置订单描述
        if(tradeBody.toString().length()>100){
            orderInfo.setTradeBody(tradeBody.toString().substring(0,100));
        }else{
            orderInfo.setTradeBody(tradeBody.toString());
        }

        //操作时间
        orderInfo.setOperateTime(new Date());
        //失效时间 +1天
        Calendar calendar = Calendar.getInstance();
        //天数加一
        calendar.add(Calendar.DATE,1);
        //设置超时时间
        orderInfo.setExpireTime(calendar.getTime());
        //设置订单进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());


        //保存订单
        orderInfoMapper.insert(orderInfo);



        //保存订单明细
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {

            //设置订单id
            orderDetail.setOrderId(orderInfo.getId());

            //添加
            orderDetailMapper.insert(orderDetail);

        }
        //删除购物车--为了测试未进行删除
//        redisTemplate.delete(RedisConst.USER_KEY_PREFIX+orderInfo.getUserId()+RedisConst.USER_CART_KEY_SUFFIX);



        //发送消息
        rabbitService.sendDelayedMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MqConst.ROUTING_ORDER_CANCEL,
                orderInfo.getId(),MqConst.DELAY_TIME);
        //返回订单id
        return orderInfo.getId();
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 生成流水号
     * 1.生成后返回页面
     * 2.生成后存储到redis
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        String tradeNo=UUID.randomUUID().toString().replaceAll("-","");
        //存储到redis
        String tradeNoKey="user:"+userId+":tradeno";
        //存储
        redisTemplate.opsForValue().set(tradeNoKey,tradeNo);

        return tradeNo;
    }

    /**
     * 校验流水号
     * @param userId
     * @param tradeNoCode
     * @return
     */
    @Override
    public boolean checkTradeCode(String userId, String tradeNoCode) {

        //获取redis中的tradeNo
        String tradeNoKey="user:"+userId+":tradeno";
        String redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);

        //判断
        if(StringUtils.isEmpty(redisTradeNo)){

            return false;
        }

        return redisTradeNo.equals(tradeNoCode);
    }

    /**
     * 删除流水号
     * @param userId
     */
    @Override
    public void deleteTradeCode(String userId) {
        //获取redis中的tradeNo
        String tradeNoKey="user:"+userId+":tradeno";
        redisTemplate.delete(tradeNoKey);
    }

    /**
     * 校验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, String skuNum) {

        //http请求http://localhost:9001/hasStock?skuId=22&num=200
        String result = HttpClientUtil.doGet(wareUrl+"/hasStock?skuId="+skuId+"&num="+skuNum);


        return "1".equals(result);
    }

    /**
     * 我的订单
     * @param orderInfoPage
     * @param userId
     * @return
     */
    @Override
    public IPage<OrderInfo> getOrderPageByUserId(Page<OrderInfo> orderInfoPage, String userId) {

        IPage<OrderInfo> orderInfoIPage = orderInfoMapper.selectOrderPageByUserId(orderInfoPage, userId);
        List<OrderInfo> records = orderInfoIPage.getRecords();
        records.stream().forEach(item->{

            //设置订单状态
            item.setOrderStatusName(OrderStatus.getStatusNameByStatus(item.getOrderStatus()));

        });

        return orderInfoIPage;
    }

    /**
     * 处理超时订单
     * @param orderId
     */
    @Override
    public void execExpiredOrder(Long orderId,String flag) {

        //关闭订单
        this.updateOrderStatus(orderId,ProcessStatus.CLOSED);

        if("2".equals(flag)){
            //发送消息取消交易记录
            this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,
                    MqConst.ROUTING_PAYMENT_CLOSE,orderId);
        }



    }

    /**
     * 修改订单状态
     * @param orderId
     * @param closed
     */
    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {

        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setId(orderId);
        //修改订单状态
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        //修改订单进度状态
        orderInfo.setProcessStatus(processStatus.name());
        orderInfoMapper.updateById(orderInfo);
    }

    /**
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //判断
        if(orderInfo!=null){

            QueryWrapper<OrderDetail> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("order_id",orderId);
            List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);
            orderInfo.setOrderDetailList(orderDetailList);


        }


        return orderInfo;
    }

    /**
     * 发送消息扣减库存
     * @param orderInfo
     */
    @Override
    public void sendOrderStatus(OrderInfo orderInfo) {
        //修改订单流程状态
        this.updateOrderStatus(orderInfo.getId(),ProcessStatus.NOTIFIED_WARE);


        //封装数据对象
        String strJson=this.initWareOrder(orderInfo);


        //发送消息
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,
                MqConst.ROUTING_WARE_STOCK,strJson);

    }

    /**
     * 封装 数据
     * @param orderInfo
     * @return
     */
    private String initWareOrder(OrderInfo orderInfo) {

        Map<String, Object> resultMap = initWareOrderMap(orderInfo);

        return JSON.toJSONString(resultMap);


    }

    /**
     * 转化数据
     * @param orderInfo
     * @return
     */
    @Override
    public Map<String, Object> initWareOrderMap(OrderInfo orderInfo) {
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("orderId",orderInfo.getId());
        resultMap.put("consignee",orderInfo.getConsignee());
        resultMap.put("consigneeTel",orderInfo.getConsigneeTel());
        resultMap.put("orderComment",orderInfo.getOrderComment());
        resultMap.put("orderBody",orderInfo.getTradeBody());
        resultMap.put("deliveryAddress",orderInfo.getDeliveryAddress());
        resultMap.put("paymentWay", "2");
        resultMap.put("wareId",orderInfo.getWareId());


        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //判断
        if(!CollectionUtils.isEmpty(orderDetailList)){
            List<OrderDetail> listMap = orderDetailList.stream().map(orderDetail -> {

                Map<String, Object> orderDetailMap = new HashMap<>();
                orderDetailMap.put("skuId", orderDetail.getSkuId());
                orderDetailMap.put("skuNum", orderDetail.getSkuNum());
                orderDetailMap.put("skuName", orderDetail.getSkuName());

                return orderDetail;
            }).collect(Collectors.toList());

            //封装订单明细
            resultMap.put("details",listMap);
        }
        return resultMap;
    }

    /**
     * 拆单
     * @param orderId
     * @param wareSkuMap
     * @return
     *
     *
     * wareSkuMap
     *  [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
     *
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {

        //创建集合封装订单信息
        List<OrderInfo> resultList=new ArrayList<>();
        //转换拆单信息数据
        //[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);

        //获取父订单对象
        OrderInfo orderOrigin = this.getOrderInfoById(Long.parseLong(orderId));
        //判断处理
        if(!CollectionUtils.isEmpty(mapList)){

            for (Map map : mapList) {
                //{"wareId":"1","skuIds":["2","10"]}
                //获取仓库id
                String wareId = (String) map.get("wareId");
                //转换
                List<String> skuIds = (List<String>) map.get("skuIds");

                //创建子订单
                OrderInfo subOrderInfo=new OrderInfo();

                //拷贝数据到子订单
                BeanUtils.copyProperties(orderOrigin,subOrderInfo);
                //置空id
                subOrderInfo.setId(null);
                //设置仓库
                subOrderInfo.setWareId(wareId);
                //设置父订单
                subOrderInfo.setParentOrderId(orderOrigin.getId());

                //设置商品明细
                //获取父订单商品总明细
                List<OrderDetail> orderDetailList = orderOrigin.getOrderDetailList();

                //创建介绍的集合
                List<OrderDetail> orderDetails = new ArrayList<>();

                StringBuilder builde=new StringBuilder();
                //判断
                if(!CollectionUtils.isEmpty(orderDetailList)){

                    for (OrderDetail orderDetail : orderDetailList) {
                        //对比是当前仓库的商品，就收集
                        for (String skuId : skuIds) {
                            //对比
                            if(Long.parseLong(skuId)==orderDetail.getSkuId().longValue()){
                                orderDetails.add(orderDetail);
                                builde.append(orderDetail.getSkuName()+" ");
                            }
                        }
                    }
                }

                subOrderInfo.setOrderDetailList(orderDetails);
                //总金额--前提必须要有商品明细
                subOrderInfo.sumTotalAmount();
                //设置商品描述
                subOrderInfo.setTradeBody(builde.toString());

                //子订单添加到数据库
                this.submitOrder(subOrderInfo);

                resultList.add(subOrderInfo);
            }
        }
        //修改父订单状态
        this.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.SPLIT);
        return resultList;
    }
}
