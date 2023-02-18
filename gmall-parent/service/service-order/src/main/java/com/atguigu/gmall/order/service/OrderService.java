package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    /**
     * 提交订单
     * @param orderInfo
     * @return
     */
    Long submitOrder(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 校验流水号
     * @param userId
     * @param tradeNoCode
     * @return
     */
    boolean checkTradeCode(String userId,String tradeNoCode);

    /**
     * 删除流水号
     * @param userId
     */
    void deleteTradeCode(String userId);


    /**
     * 校验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId,String skuNum);


    /**
     * 我的订单
     * @param orderInfoPage
     * @param userId
     * @return
     */
    IPage<OrderInfo> getOrderPageByUserId(Page<OrderInfo> orderInfoPage, String userId);

    /**
     * 处理超时订单
     * @param orderId
     */
    void execExpiredOrder(Long orderId,String flag);

    /**
     * 修改订单状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    /**
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoById(Long orderId);

    /**
     * 发送消息扣减库存
     * @param orderInfo
     */
    void sendOrderStatus(OrderInfo orderInfo);

    /**
     * 转换数据
     * @param orderInfo
     * @return
     */
     Map<String, Object> initWareOrderMap(OrderInfo orderInfo);

    /**
     * 拆单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
