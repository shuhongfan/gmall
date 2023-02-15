package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/product/baseCategoryTrademark")
public class BaseCategoryTrademarkController {

    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;


    @PostMapping("save")
    public Result save(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        //  保存方法
        baseCategoryTrademarkService.save(categoryTrademarkVo);
        return Result.ok();
    }

    @DeleteMapping("remove/{category3Id}/{trademarkId}")
    public Result remove(@PathVariable Long category3Id, @PathVariable Long trademarkId){
        //  调用服务层方法
        baseCategoryTrademarkService.remove(category3Id, trademarkId);
        return Result.ok();
    }


    @GetMapping("findCurrentTrademarkList/{category3Id}")
    public Result findCurrentTrademarkList(@PathVariable Long category3Id){
        List<BaseTrademark> list = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        //  返回
        return Result.ok(list);
    }

    /**
     * 查询全部品牌
     * @return
     */
    @GetMapping("findTrademarkList/{category3Id}")
    public Result findTrademarkList(@PathVariable Long category3Id){
        //  调用服务层方法
        List<BaseTrademark> baseCategoryTrademarkList = baseCategoryTrademarkService.findTrademarkList(category3Id);
        //  返回数据
        return Result.ok(baseCategoryTrademarkList);
    }

}

