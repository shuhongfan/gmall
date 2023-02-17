package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 通过品牌Id 来查询数据
     * @param tmId
     * @return
     */
    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }

    /**
     * 获取全部分类信息
     * @return
     */
    @GmallCache(prefix = "category")
    @Override
    public List<JSONObject> getBaseCategoryList() {
        ArrayList<JSONObject> resultList = new ArrayList<>();

//        查询所以三级分类
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

//        分组处理
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

//        定义一级分类的序号
        int index = 1;

//        分组后处理一级分类数据
        for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {
//            获取一级分类
            Long category1Id = entry.getKey();

//            获取一级分类名称
            List<BaseCategoryView> category2List = entry.getValue();
            String category1Name = category2List.get(0).getCategory1Name();

//            创建对象
            JSONObject category1Json = new JSONObject();
            category1Json.put("index", index++);
            category1Json.put("categoryName", category1Name);
            category1Json.put("categoryId", category1Id);

//            创建处理二级分类的的集合
            ArrayList<JSONObject> categoryChild2 = new ArrayList<>();

//            处理二级分类
            Map<Long, List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : category2Map.entrySet()) {
//                二级分类ID
                Long category2Id = category2Entry.getKey();

//                二级分类的名称
                List<BaseCategoryView> category3Result = category2Entry.getValue();
                String category2Name = category3Result.get(0).getCategory2Name();

//                创建二级分类对象封装
                JSONObject category2Json = new JSONObject();
                category2Json.put("categoryName", category2Name);
                category2Json.put("categoryId", category2Id);

//                创建集合收集三级分类
                ArrayList<JSONObject> categoryChild3 = new ArrayList<>();

//                处理三级分类
                for (BaseCategoryView category3: category3Result) {
//                    创建三级分类的对象
                    JSONObject category3JSON = new JSONObject();

                    category3JSON.put("categoryId", category3.getCategory3Id());
                    category3JSON.put("categoryName", category3.getCategory3Name());

//                    添加到集合
                    categoryChild3.add(category3JSON);
                }

//                存储到二级分类的categoryChild
                category2Json.put("categoryChild", categoryChild3);
            }

//                存储到一级分类的categoryChild
            category1Json.put("categoryChild", categoryChild2);

//            添加到总结过中
            resultList.add(category1Json);
        }


        return resultList;
    }

    /**
     * 根据spuId 查询map 集合属性
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "saleAttrValuesBySpu:")
    public Map getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
        // key = 125|123 ,value = 37
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        if (mapList != null && mapList.size() > 0) {
            // 循环遍历
            for (Map skuMap : mapList) {
                // key = 125|123 ,value = 37
                map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
            }
        }
        return map;
    }

    /**
     * 根据spuId，skuId 查询销售属性集合
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "spuSaleAttrListCheckBySku:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * 根据spuId 查询spuImageList
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        LambdaQueryWrapper<SpuImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuImage::getSpuId, spuId);
        return spuImageMapper.selectList(wrapper);
    }

    /**
     * 查询所有的销售属性数据
     * @return
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * spu分页查询
     * @param pageParam
     * @param spuInfo
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        LambdaQueryWrapper<SpuInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpuInfo::getCategory3Id, spuInfo.getCategory3Id());
        wrapper.orderByDesc(SpuInfo::getId);

        return spuInfoMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 根据attrId 查询平台属性对象
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);

//        根据属性id获取属性值
        LambdaQueryWrapper<BaseAttrValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseAttrValue::getAttrId, attrId);
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.selectList(wrapper);

        // 查询到最新的平台属性值集合数据放入平台属性中！
        baseAttrInfo.setAttrValueList(attrValueList);
        return baseAttrInfo;
    }
    /**
     * 保存平台属性方法
     * @param baseAttrInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
//        判断当前操作是保存还是修改
        if (baseAttrInfo.getId() != null) {
//            修改平台属性
            baseAttrInfoMapper.updateById(baseAttrInfo);
        } else {
//            保存平台属性
            baseAttrInfoMapper.insert(baseAttrInfo);

        }

        LambdaQueryWrapper<BaseAttrValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseAttrValue::getAttrId, baseAttrInfo.getId());
        baseAttrValueMapper.delete(wrapper);

        // 获取页面传递过来的所有平台属性值数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
//            给属性值表添加数据
            attrValueList.stream().forEach(attrValue -> {
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(attrValue);
            });
        }
    }

    /**
     * 查询所有的一级分类信息
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 根据一级分类Id 查询二级分类数据
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        LambdaQueryWrapper<BaseCategory2> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategory2::getCategory1Id, category1Id);
        return baseCategory2Mapper.selectList(wrapper);
    }

    /**
     * 根据二级分类Id 查询三级分类数据
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        LambdaQueryWrapper<BaseCategory3> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseCategory3::getCategory2Id, category2Id);
        return baseCategory3Mapper.selectList(wrapper);
    }

    /**
     * 根据分类Id 获取平台属性数据
     * 接口说明：
     *      1，平台属性可以挂在一级分类、二级分类和三级分类
     *      2，查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性
     *      3，查询二级分类下面的平台属性，传：category1Id，category2Id，0；
     *         取出对应一级分类下面的平台属性与二级分类对应的平台属性
     *      4，查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；
     *         取出对应一级分类、二级分类与三级分类对应的平台属性
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    /**
     * 保存商品数据
     * @param spuInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insert(spuInfo);

//        获取图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)) {
            spuImageList.forEach(spuImage -> {
                spuImage.setSpuId(spuInfo.getId());
//                保存spuImage
                spuImageMapper.insert(spuImage);
            });
        }

//        获取销售属性集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

//                获取销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });
                }
            });
        }

//        获取到posterList集合数据
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            spuPosterList.forEach(spuPoster -> {
                spuPoster.setSpuId(spuInfo.getId());
                spuPosterMapper.insert(spuPoster);
            });
        }

    }

    /**
     * 根据spuId 查询销售属性集合
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    /**
     * 保存数据
     * @param skuInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.insert(skuInfo);

//        图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }

//        销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }

//        告诉布隆过滤器，存储是否存在的标记到布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);

//        添加数据
        bloomFilter.add(skuInfo.getId());
    }

    /**
     * SKU分页列表
     * @param pageParam
     * @return
     */
    @Override
    public IPage<SkuInfo> getPage(Page<SkuInfo> pageParam) {
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SkuInfo::getId);
        return skuInfoMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 商品上架
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    /**
     * 商品下架
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    /**
     * 根据skuId 查询skuInfo
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = RedisConst.SKUKEY_PREFIX)
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

//        根据SKUID查询图片列表集合
        LambdaQueryWrapper<SkuImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuImage::getSkuId, skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(wrapper);

        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }

    /**
     * 通过三级分类id查询分类信息
     * @param category3Id
     * @return
     */
    @Override
    @GmallCache(prefix = "categoryViewByCategory3Id:")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 获取sku价格
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        RLock lock = redissonClient.getLock(skuId + ":lock");
//        加锁
        lock.lock();
        SkuInfo skuInfo = null;
        BigDecimal price = new BigDecimal(0);

        try {
            LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SkuInfo::getId, skuId);
            wrapper.select(SkuInfo::getPrice);
            skuInfo = skuInfoMapper.selectOne(wrapper);
            if (skuInfo != null) {
                price = skuInfo.getPrice();
            }
        } finally {
//            解锁
            lock.unlock();
        }

        return price;
    }

    /**
     * 根据spuid获取商品海报
     * @param spuId
     * @return
     */
    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        LambdaQueryWrapper<SpuPoster> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpuPoster::getSpuId, spuId);
        return spuPosterMapper.selectList(queryWrapper);
    }

    /**
     * 通过skuId 集合来查询数据
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = "BaseAttrInfoList:")
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }

}
