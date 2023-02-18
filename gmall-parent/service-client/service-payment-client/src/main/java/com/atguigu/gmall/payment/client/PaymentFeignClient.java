package com.atguigu.gmall.payment.client;

import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.client.impl.PaymentDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(value = "service-payment" ,fallback = PaymentDegradeFeignClient.class)
public interface PaymentFeignClient {


    /**
     * 查询支付记录信息
     * @param outTradeNo
     * @return
     */
    @GetMapping("/api/payment/alipay/getPaymentInfo/{outTradeNo}")
    @ResponseBody
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo);

    /**
     * http://localhost:8205/api/payment/alipay/closePay/25
     * 支付宝关闭交易
     * @param orderId
     * @return
     */
    @GetMapping("/api/payment/alipay/closePay/{orderId}")
    @ResponseBody
    public Boolean closePay(@PathVariable Long orderId);

    /**
     * http://localhost:8205/api/payment/alipay/checkPayment/30
     * 查询支付宝交易记录
     * @param orderId
     * @return
     */
    @GetMapping("/api/payment/alipay/checkPayment/{orderId}")
    @ResponseBody
    public Boolean checkPayment(@PathVariable Long orderId);



}
