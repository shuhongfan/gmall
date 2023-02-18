package com.atguigu.gmall.list.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public Result list(SearchParam searchParam) {
        return Result.fail();
    }

    @Override
    public Result incrHotScore(Long skuId) {
        return Result.fail();
    }
}
