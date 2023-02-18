package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class SpuManagerController {


    @Autowired
    private ManagerService managerService;



    /**
     * admin/product/saveSpuInfo
     * 保存spu
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){


        managerService.saveSpuInfo(spuInfo);

        return Result.ok();

    }

    /**
     * admin/product/baseSaleAttrList
     * 获取销售属性
     *
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){

       List<BaseSaleAttr> baseSaleAttrList= managerService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    /**
     * admin/product/{page}/{limit}
     * 根据三级分类分页查询spu列表
     * @return
     */
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo){

        //封装参数
        Page<SpuInfo> infoPage=new Page<>(page,limit);
        //结果
        IPage<SpuInfo> infoIPage=managerService.getSpuInfoPage(spuInfo,infoPage);

        return Result.ok(infoIPage);
    }
}
