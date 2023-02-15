package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 根据三级分类获取品牌
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
//        根据分类id获取品牌id集合数据
        LambdaQueryWrapper<BaseCategoryTrademark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategoryTrademark::getCategory3Id, category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(wrapper);

        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
//            获取到这个集合中的品牌Id 集合数据
            List<Long> tradeMarkIdList = baseCategoryTrademarkList.stream()
                    .map(BaseCategoryTrademark::getTrademarkId)
                    .collect(Collectors.toList());
            return baseTrademarkMapper.selectBatchIds(tradeMarkIdList);
        }
        return null;
    }

    /**
     * 保存分类与品牌关联
     * @param categoryTrademarkVo
     */
    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
//        获取品牌id集合
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();

//        遍历处理 封装成对象
        List<BaseCategoryTrademark> baseCategoryTrademarkList = trademarkIdList.stream().map(trademarkId -> {
            BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
            baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
            baseCategoryTrademark.setTrademarkId(trademarkId);
            return baseCategoryTrademark;
        }).collect(Collectors.toList());

        saveBatch(baseCategoryTrademarkList);
    }

    /**
     * 获取当前未被三级分类关联的所有品牌
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        List<BaseTrademark> baseTrademarkList = new ArrayList<>();

//        根据三级分类id查询中间表获取关联的品牌id
        LambdaQueryWrapper<BaseCategoryTrademark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategoryTrademark::getCategory3Id, category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(wrapper);

        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
//            获取当前分类关联的品牌id集合
            List<Long> tradeMarkIdList = baseCategoryTrademarkList.stream()
                    .map(BaseCategoryTrademark::getTrademarkId)
                    .collect(Collectors.toList());

//            查询所有品牌
            List<BaseTrademark> trademarkList = baseTrademarkMapper.selectList(null);

            baseTrademarkList = trademarkList.stream()
                    .filter(baseTrademark -> !tradeMarkIdList.contains(baseTrademark.getId()))
                    .collect(Collectors.toList());

//            LambdaQueryWrapper<BaseTrademark> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.notIn(BaseTrademark::getId, tradeMarkIdList);
//            baseTrademarkList = baseTrademarkMapper.selectList(queryWrapper);
        }
        return baseTrademarkList;
    }

    /**
     * 删除关联
     * @param category3Id
     * @param trademarkId
     */
    @Override
    public void remove(Long category3Id, Long trademarkId) {
        LambdaQueryWrapper<BaseCategoryTrademark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategoryTrademark::getCategory3Id, category3Id);
        wrapper.eq(BaseCategoryTrademark::getTrademarkId, trademarkId);

        baseCategoryTrademarkMapper.delete(wrapper);
    }
}
