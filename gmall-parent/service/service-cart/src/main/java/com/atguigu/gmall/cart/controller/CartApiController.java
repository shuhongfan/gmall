package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {


    @Autowired
    private CartService cartService;



    /**
     * api/cart/getCartCheckedList/{userId}
     * 获取选中状态的购物车列表
     * @param userId
     * @return
     */
    @GetMapping("/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId){


        return cartService.getCartCheckedList(userId);

    }



    /**
     * api/cart/deleteCart/{skuId}
     * 删除购物车
     * @return
     */
    @DeleteMapping ("/deleteCart/{skuId}")
    public  Result deleteCart(@PathVariable Long skuId, HttpServletRequest request){

        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //判断
        if(StringUtils.isEmpty(userId)){

            userId=AuthContextHolder.getUserTempId(request);
        }
        //删除购物车
        cartService.deleteCart(userId,skuId);



        return Result.ok();
    }
    /**
     * 更新选中状态
     * api/cart/checkCart/{skuId}/{isChecked}
     * @param skuId
     * @param isChecked
     * @return
     */
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request){

        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //判断
        if(StringUtils.isEmpty(userId)){

            userId=AuthContextHolder.getUserTempId(request);
        }

        //实现状态的更改
        cartService.checkCart(userId,skuId,isChecked);


        return Result.ok();


    }

    /**
     * api/cart/cartList
     * 展示购物车
     * @return
     */
    @GetMapping("/cartList")
    public Result cartList(HttpServletRequest request){

        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //获取用户临时id
        String userTempId = AuthContextHolder.getUserTempId(request);
        //查询购物车
        List<CartInfo> cartInfoList=this.cartService.cartList(userId,userTempId);


        return Result.ok(cartInfoList);
    }


    /**
     * /api/cart/addToCart/{skuId}/{skuNum}
     * 加入购物车
     * @return
     */
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request){

        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //判断
        if(StringUtils.isEmpty(userId)){
            userId=AuthContextHolder.getUserTempId(request);
        }

        //加入购物车
        cartService.addToCart(skuId,skuNum,userId);


        return Result.ok();
    }
}
