package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManagerService;
import com.atguigu.gmall.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")

public class ManagerServiceImpl implements ManagerService {


    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private  SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;


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
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;

    /**
     * 查询一级分类
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {

        //条件设置为null,表示查询所有
        //select*from base_category1
        List<BaseCategory1> baseCategory1List = baseCategory1Mapper.selectList(null);


        return baseCategory1List;
    }

    /**
     * 根据一级分类id查询二级分类数据
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {

        //select *from base_category2 where category1_id=category1Id

        //创建查询条件
        QueryWrapper<BaseCategory2> queryWrapper=new QueryWrapper<>();
        //添加条件
        queryWrapper.eq("category1_id",category1Id);

        //查询结果
        return baseCategory2Mapper.selectList(queryWrapper);
    }

    /**
     * 根据二级分类查询三级分类数据
     * @param category2Id
     */
    @Override
    public  List<BaseCategory3>  getCategory3(Long category2Id) {

        //select *from base_category3 where category2_id=category1Id

        //创建查询条件
        QueryWrapper<BaseCategory3> queryWrapper=new QueryWrapper<>();
        //添加条件
        queryWrapper.eq("category2_id",category2Id);

        //查询结果
        return baseCategory3Mapper.selectList(queryWrapper);
    }

    /**
     * 根据分类id查询平台属性
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {

        //调用mapper查询
        return  baseAttrInfoMapper.selectAttrInfoList(category1Id,category2Id,category3Id);
    }

    /**
     * 新增和修改平台属性
     * @param baseAttrInfo
     *
     *  Transactional：
     *      默认配置的方式：只能对运行时异常进行回滚
     *       RuntimeException
     *
     *  rollbackFor = Exception.class
     *    IOException
     *    SQLException
     *
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //判断当前操作是否保存还是修改
        if(baseAttrInfo.getId()!=null){
            //修改平台属性
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //根据平台属性删除属性值集合 逻辑删除 ，sql文件物理删除

            //UPDATE base_attr_value SET is_deleted=1 WHERE is_deleted=0 AND (attr_id = ?)
            //创建删除条件对象
            QueryWrapper<BaseAttrValue> wrapper=new QueryWrapper<>();
            wrapper.eq("attr_id",baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);


        }else{
            //保存平台属性
            baseAttrInfoMapper.insert(baseAttrInfo);

        }



        //操作平台属性值
        //新增，获取平台属性值集合
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();


        //判断
        if(!CollectionUtils.isEmpty(attrValueList)){
            for (BaseAttrValue baseAttrValue : attrValueList) {

                //设置平台属性id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                //保存
                baseAttrValueMapper.insert(baseAttrValue);


            }


        }


    }

    /**
     * 根据属性id查询属性对象
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {

        //获取属性对象
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        //获取属性值集合
        List<BaseAttrValue> list=getAttrValueList(attrId);
        //设置属性值集合
        baseAttrInfo.setAttrValueList(list);

        return baseAttrInfo;
    }

    /**
     * 根据三级分类分页查询spu列表
     * @param spuInfo
     * @param infoPage
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfoPage(SpuInfo spuInfo, Page<SpuInfo> infoPage) {
        //创建条件对象
        QueryWrapper<SpuInfo> queryWrapper=new QueryWrapper<>();
        //设置条件
        queryWrapper.eq("category3_id",spuInfo.getCategory3Id());

        return spuInfoMapper.selectPage(infoPage,queryWrapper);
    }

    /**
     *  获取销售属性
     * @return
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 保存spu
     * @param spuInfo
     *
     *  spuInfo涉及到的表
     * spu_info 基本信息表
     * spu_image 图片表
     * spu_poster 海报表
     * spu_sale_attr_value 销售属性值表
     * spu_sale_attr  销售属性表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)


    public void saveSpuInfo(SpuInfo spuInfo) {


        //保存spu信息
        spuInfoMapper.insert(spuInfo);

        //保存图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //判断
        if(!CollectionUtils.isEmpty(spuImageList)){

            for (SpuImage spuImage : spuImageList) {

                //设置spuId
                spuImage.setSpuId(spuInfo.getId());
                //保存图片到数据库
                spuImageMapper.insert(spuImage);

            }



        }

        //保存海报
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        //判断
        if(!CollectionUtils.isEmpty(spuPosterList)){
            for (SpuPoster spuPoster : spuPosterList) {

                //设置spuId
                spuPoster.setSpuId(spuInfo.getId());
                //保存海报
                spuPosterMapper.insert(spuPoster);
            }


        }


        //保存销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //判断
        if(!CollectionUtils.isEmpty(spuSaleAttrList)){

            //保存销售属性
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {

                //设置spuId
                spuSaleAttr.setSpuId(spuInfo.getId());
                //保存
                spuSaleAttrMapper.insert(spuSaleAttr);

                //获取销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                //判断
                if(!CollectionUtils.isEmpty(spuSaleAttrValueList)){

                    //保存销售属性值
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        //设置spuId
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        //设置销售属性名称
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        //保存销售属性值
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);




                    }


                }


            }


        }


    }

    /**
     * 根据spuId查询销售属性和销售属性值集合
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {

        return  spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    /**
     * 根据spuId查询图片列表
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> spuImageList(Long spuId) {

        //创建条件对象
        QueryWrapper<SpuImage> queryWrapper=new QueryWrapper<>();
        //select *from spu_image where spuid=spuId
        queryWrapper.eq("spu_id",spuId);

        return  spuImageMapper.selectList(queryWrapper);
    }


    /**
     * 保存skuInfo
     *  操作的表：
     *   sku_info 基本信息表
     *   sku_image sku图片表
     *   sku_sale_attr_value sku销售属性表
     *   sku_attr_value sku品台属性表
     *
     * @param skuInfo
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {


        //设置is_sale
        skuInfo.setIsSale(0);
        //保存skuinfo
        skuInfoMapper.insert(skuInfo);
        //保存图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        //判断
        if(!CollectionUtils.isEmpty(skuImageList)){

            for (SkuImage skuImage : skuImageList) {

                //设置skuid
                skuImage.setSkuId(skuInfo.getId());
                //保存
                skuImageMapper.insert(skuImage);

            }

        }

        //保存平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //判断
        if(!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {

                //设置skuId
                skuAttrValue.setSkuId(skuInfo.getId());
                //保存
                skuAttrValueMapper.insert(skuAttrValue);
            }


        }


        //保存销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //判断
        if(!CollectionUtils.isEmpty(skuSaleAttrValueList)){

            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

                //设置skuId
                skuSaleAttrValue.setSkuId(skuInfo.getId());

                //设置spuId
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                //保存
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);

            }

        }


        //告诉布隆过滤器，存储是否在的标记到布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        //添加数据
        bloomFilter.add(skuInfo.getId());

    }

    /**
     * sku分页列表
     * @param skuInfoPage
     * @return
     */
    @Override
    public IPage<SkuInfo> skuListPage(Page<SkuInfo> skuInfoPage) {

        //排序
        QueryWrapper<SkuInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("id");


        return skuInfoMapper.selectPage(skuInfoPage,queryWrapper);
    }

    /**
     * 商品上架
     * 将is_sale改为1
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {

        //封装对象
        SkuInfo skuInfo=new SkuInfo();
        //设置条件
        skuInfo.setId(skuId);
        //设置修改的内容
        skuInfo.setIsSale(1);


        skuInfoMapper.updateById(skuInfo);


        //发送消息
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,
                MqConst.ROUTING_GOODS_UPPER,skuId);

    }

    /**
     * 商品的下架
     *  将is_sale改为0
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        //封装对象
        SkuInfo skuInfo=new SkuInfo();
        //设置条件
        skuInfo.setId(skuId);
        //设置修改的内容
        skuInfo.setIsSale(0);

        skuInfoMapper.updateById(skuInfo);

        //发送消息
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_LOWER,skuId);
    }

    /**
     * 根据skuId查询skuInfo信息和图片列表
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix="sku:")
    public SkuInfo getSkuInfo(Long skuId) {

        //使用redis实现分布式锁缓存数据
//        return getSkuInfoRedis(skuId);

//        return getSkuInfoRedisson(skuId);
        //查询数据库mysql获取数据
        return getSkuInfoDB(skuId);
    }

    @Autowired
    private  RedissonClient redissonClient;

    /**
     * 使用reddisson改造skuinfo信息获取
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedisson(Long skuId) {

        try {
            //定义sku数据获取的key
            String skuKey=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            //从缓冲中获取数据数据
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            //判断是否获取了数据
            if(skuInfo==null){
                //定义锁的key
                String  skuLock=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                //获取锁
                RLock lock = redissonClient.getLock(skuLock);
                //加锁
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断
                if(res){

                    try {
                        //获取到了锁,查询数据库
                        skuInfo = getSkuInfoDB(skuId);
                        //判断
                        if(skuInfo==null){
                            //存储null
                            skuInfo=new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TEMPORARY_TIMEOUT);
                            return skuInfo;

                        }else{

                            //存储
                            redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT);
                            //返回
                            return skuInfo;

                        }
                    } finally {

                        //释放锁
                        lock.unlock();
                    }


                }else{
                    //没有获取到锁
                    Thread.sleep(100);
                    return getSkuInfoRedisson(skuId);


                }



            }else{

                //缓存中有数据
                return skuInfo;

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //兜底的方法
        return getSkuInfoDB(skuId);
    }

    /**
     * 获取skuIfo从缓存中获取数据
     * redis实现分布式锁
     * @param skuId
     * @return
     *
     *  实现步骤：
     *   1.定义存储skuinf的key
     *   2.根据skuKey获取skuinfo的缓存数据
     *   3.判断
     *      有  直接返回结束
     *      没有
     *        定义锁的key
     *        尝试加锁
     *          失败  睡眠，重试自旋
     *          成功 查询数据库
     *          判断是否有值
     *           有，直接返回，缓存到数据库
     *           没有，创建空值，返回数据
     *          释放锁
     *
     *
     *
     */
    private SkuInfo getSkuInfoRedis(Long skuId) {

        try {
            //定义存储skuKey sku:1314:info
            String skuKey=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            //尝试获取缓存中的数据
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //判断是否有值
            if(skuInfo==null){
                //说明缓存中没有数据
                //定义锁的key
                String lockKey=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                //生成uuid标识
                String uuid = UUID.randomUUID().toString().replaceAll("-","");
                //获取锁
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断是否获取到了锁
                if(flag){ //获取 到了锁

                    //查询数据库
                    SkuInfo skuInfoDB = getSkuInfoDB(skuId);
                    //判断数据中是否有值
                    if(skuInfoDB==null){
                        SkuInfo skuInfo1=new SkuInfo();

                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        return skuInfo1;
                    }
                        //数据库查询的数据不为空
                        //存储到缓存
                        redisTemplate.opsForValue().set(skuKey,skuInfoDB,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);





                    //释放锁-lua脚本
                    //定义lua脚本
                    String script="if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                            "then\n" +
                            "    return redis.call(\"del\",KEYS[1])\n" +
                            "else\n" +
                            "    return 0\n" +
                            "end";
                    //创建脚本对象
                    DefaultRedisScript<Long> defaultRedisScript=new DefaultRedisScript<>();
                    //设置脚本
                    defaultRedisScript.setScriptText(script);
                    //设置返回 值类型
                    defaultRedisScript.setResultType(Long.class);


                    //执行删除
                    redisTemplate.execute(defaultRedisScript, Arrays.asList(lockKey),uuid);

                    //返回数据
                    return skuInfoDB;

                }else{
                    Thread.sleep(100);
                    return getSkuInfoRedis(skuId);

                }

            }else{

                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        //兜底，--在上面从缓存中获取的过程中出现异常，这行代码也必须执行。。。
        return getSkuInfoDB(skuId);
    }

    /**
     * 查询数据库获取skuInfo信息
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoDB(Long skuId) {
        //查询skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //根据skuId查询图片列表
        //查询条件对象
        //select *from sku_image where sku_id=skuId
        QueryWrapper<SkuImage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);

        List<SkuImage> skuImages = skuImageMapper.selectList(queryWrapper);
        //设置图片列表
        if(skuInfo!=null){
            skuInfo.setSkuImageList(skuImages);

        }



        return skuInfo;
    }



    /**
     * 根据三级分类id获取分类信息
     * @param category3Id
     * @return
     */
    @Override
    @GmallCache(prefix="categoryView:")
    public BaseCategoryView getCategoryView(Long category3Id) {


        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 根据skuId查询sku实时价格
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {


        //获取锁
        RLock lock = redissonClient.getLock(skuId + ":lock");

        try {
            //加锁
            lock.lock();
            //查询skuInfo对象
            SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
            //判断
            if(skuInfo!=null){

                return skuInfo.getPrice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            lock.unlock();
        }

        return new BigDecimal("0");
    }

    /**
     * 根据spuId,skuId 获取销售属性数据
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix="spuSaleAttrListCheckBySku:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList= spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);

        return spuSaleAttrList ;
    }

    /**
     * 根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix="skuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        //查询对应关系集合
        List<Map> mapList = skuSaleAttrValueMapper.selectSkuValueIdsMap(spuId);
        //创建map整合返回的数据
        Map<Object ,Object> resultMap= new HashMap();

        //判断
        if(!CollectionUtils.isEmpty(mapList)){

            for (Map map : mapList) {
                //map 只有一条数据
                //整合所有 key 属性id拼接的结果  value skuId
                resultMap.put(map.get("value_ids"),map.get("sku_id"));

            }
        }

        return resultMap;
    }

    /**
     * 根据spuid查询海报集合数据
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix="findSpuPosterBySpuId:")
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {

        //查询sql select *from spu_poster where  spu_id=spuId
        //创建条件对象
        QueryWrapper<SpuPoster> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        //执行查询
        List<SpuPoster> posterList = spuPosterMapper.selectList(queryWrapper);

        return posterList;
    }

    /**
     * 根据skuId 获取平台属性数据
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix="attrList:")
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectAttrList(skuId);
    }

    /**
     *首页数据查询三级分类数
     * @return
     */
    @Override
    @GmallCache(prefix = "baseCategoryList:")
    public List<JSONObject> getBaseCategoryList() {
        //创建对象，封装结果
        List<JSONObject> resultList=new ArrayList<>();
        //查询所有三级分类
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //分组处理   key:一级分类的id value:一级分类对应的所有数据
        Map<Long, List<BaseCategoryView>> category1Map =
                baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        //定义一级分类的序号
        int index=1;

        //分组后处理一级分类数据
        for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {

            //每一个entry，是一个键值对  key:一级分类的id value:一级分类对应的所有数据
            //获取一级分类
            Long category1Id = entry.getKey();
            //获取一级分类名称
            List<BaseCategoryView> category2List = entry.getValue();
            String category1Name = category2List.get(0).getCategory1Name();
            //创建对象
            JSONObject category1Json=new JSONObject();
            category1Json.put("index",index++);
            category1Json.put("categoryName",category1Name);
            category1Json.put("categoryId",category1Id);

            //处理二级分类
            Map<Long, List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));


            //创建一个封装二级分类的集合
            List<JSONObject> categoryChild2=new ArrayList<>();

            //遍历
            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : category2Map.entrySet()) {

                //二级分类的id
                Long category2Id = category2Entry.getKey();
                //二级份分类的名称
                List<BaseCategoryView> category3Result = category2Entry.getValue();
                String category2Name = category3Result.get(0).getCategory2Name();
                //创建二级分类对象封装
                JSONObject category2Json=new JSONObject();
                category2Json.put("categoryId",category2Id);
                category2Json.put("categoryName",category2Name);

                //创建集合收集三级分类
                List<JSONObject> categoryChild3=new ArrayList<>();

                //处理三级分类
                for (BaseCategoryView baseCategoryView : category3Result) {

                    //创建三级分类的对象
                    JSONObject category3Json=new JSONObject();

                    category3Json.put("categoryId",baseCategoryView.getCategory3Id());
                    category3Json.put("categoryName",baseCategoryView.getCategory3Name());

                    //添加到集合
                    categoryChild3.add(category3Json);
                }

                //存储到二级分类的categoryChild字段
                category2Json.put("categoryChild",categoryChild3);

                //收集到二级分类集合中
                categoryChild2.add(category2Json);

            }

            //添加到一级分类的categoryChild
            category1Json.put("categoryChild",categoryChild2);


            //添加总结果中
            resultList.add(category1Json);
        }


        return resultList;
    }

    /**
     * 根据属性id查询属性值集合
     * @param attrId
     * @return
     */

    private List<BaseAttrValue> getAttrValueList(Long attrId) {

        //创建条件对象
        QueryWrapper<BaseAttrValue> wrapper=new QueryWrapper<>();
        wrapper.eq("attr_id",attrId);
        //查询数据
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(wrapper);


        return baseAttrValueList;
    }
}
