package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@SuppressWarnings("all")
public class SeckillController {

    @Autowired
    private ActivityFeignClient activityFeignClient;

    /**
     * 跳转进行抢单
     * @return
     */
  @GetMapping("seckill/queue.html")
  public String queue(HttpServletRequest request){
      String skuId = request.getParameter("skuId");
      String skuIdStr = request.getParameter("skuIdStr");
      request.setAttribute("skuId",skuId);
      request.setAttribute("skuIdStr",skuIdStr);

      return "seckill/queue";
  }



    /**
     * 秒杀商品列表
     * @param model
     * @return
     */
    @GetMapping("/seckill.html")
    public String index(Model model){
        Result result = activityFeignClient.findAll();

        model.addAttribute("list",result.getData());
        return "seckill/index";

    }

    /**
     * 秒杀商品详情
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/seckill/{skuId}.html")
    public String item(@PathVariable Long skuId, Model model){
        Result result = activityFeignClient.getSeckillGoods(skuId);

        model.addAttribute("item",result.getData());
        return "seckill/item";

    }

    /**
     * 确认订单
     * @param model
     * @return
     */
    @GetMapping("seckill/trade.html")
    public String trade(Model model) {
        Result<Map<String, Object>> result = activityFeignClient.trade();
        if(result.isOk()) {
            model.addAllAttributes(result.getData());
            return "seckill/trade";
        } else {
            model.addAttribute("message",result.getMessage());

            return "seckill/fail";
        }
    }

}
