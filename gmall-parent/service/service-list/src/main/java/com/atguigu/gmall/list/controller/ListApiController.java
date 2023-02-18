package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/list")
public class ListApiController {





    @Autowired
    private ElasticsearchRestTemplate restTemplate;


    @Autowired
    private SearchService searchService;



    /**
     * api/list
     * 商品搜索
     * @return
     */
    @PostMapping
    public Result list(@RequestBody SearchParam searchParam){


        SearchResponseVo searchResponseVo=searchService.search(searchParam);

        return Result.ok(searchResponseVo);
    }


    /**
     * api/list/inner/incrHotScore/{skuId}
     * 更新商品的热度排名
     * @param skuId
     * @return
     */
    @GetMapping("/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId){

        searchService.incrHotScore(skuId);

        return Result.ok();
    }

    /**
     * 商品下架
     * api/list/inner/lowerGoods/{skuId}
     * @param skuId
     * @return
     */
    @GetMapping("/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){

        searchService.lowerGoods(skuId);
        return Result.ok();
    }
    /**
     * /api/list/inner/upperGoods/{skuId}
     * 商品上架
     * @param skuId
     * @return
     */
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){

        searchService.upperGoods(skuId);

        return Result.ok();
    }


    /**
     *    ///api/list/createIndex
     * 创建索引库，建立mapping结构
     * @return
     */
    @GetMapping("createIndex")
    public Result createIndex(){
        //创建索引库
        restTemplate.createIndex(Goods.class);
        //建立mapping
        restTemplate.putMapping(Goods.class);


        return Result.ok();

    }
}
