package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.ManagerService;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product/inner")
public class ProductApiController {

    @Autowired
    private ManagerService managerService;


    @Autowired
    private BaseTrademarkService trademarkService;



    /**
     * 根据品牌Id 获取品牌数据
     * api/product/inner/getTrademark/{tmId}
     * @param tmId
     * @return
     */
    @GetMapping("/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable Long tmId){


        return trademarkService.getById(tmId);

    }

    /**
     * 首页数据查询三级分类数
     * api/product/inner/getBaseCategoryList
     *
     * @return
     */
    @GetMapping("/getBaseCategoryList")
    public Result getBaseCategoryList(){

        List<JSONObject> result=managerService.getBaseCategoryList();


        return Result.ok(result);
    }


    /**
     *
     * 根据skuId 获取平台属性数据
     * api/product/inner/getAttrList/{skuId}
     * @param skuId
     * @return
     */
    @GetMapping("/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){


        return managerService.getAttrList(skuId);

    }


    /**
     * 根据spuid查询海报集合数据
     * api/product/inner/findSpuPosterBySpuId/{spuId}
     * @param spuId
     * @return
     */
    @GetMapping("/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId){


        return managerService.findSpuPosterBySpuId(spuId);

    }


    /**
     * api/product/inner/getSkuValueIdsMap/{spuId}
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     */
    @GetMapping("/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){


       return managerService.getSkuValueIdsMap(spuId);


    }

    /**
     * 根据spuId,skuId 获取销售属性数据
     * api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){


        return managerService.getSpuSaleAttrListCheckBySku(skuId,spuId);


    }


    /**
     * api/product/inner/getSkuPrice/{skuId}
     * 根据skuId查询sku实时价格
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){


       return  managerService.getSkuPrice(skuId);
    }



    /**
     *根据三级分类id获取分类信息
     * api/product/inner/getCategoryView/{category3Id}
     *
     * @param category3Id
     * @return
     */
    @GetMapping("/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){


        return managerService.getCategoryView(category3Id);

    }



    /**
     * /api/product/inner/getSkuInfo/{skuId}
     * 根据skuId查询skuInfo信息和图片列表
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){

         SkuInfo skuInfo=managerService.getSkuInfo(skuId);
        return skuInfo;
    }
}
