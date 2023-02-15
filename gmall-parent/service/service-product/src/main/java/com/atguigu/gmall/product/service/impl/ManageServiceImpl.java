package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
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
}
