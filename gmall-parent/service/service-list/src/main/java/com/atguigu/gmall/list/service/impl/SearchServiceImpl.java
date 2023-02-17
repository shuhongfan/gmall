package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    /**
     * 上架商品列表
     *
     * @param skuId
     */
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();

//        查询SKU对应的平台属性
        List<BaseAttrInfo> baseAttrInfoList = productFeignClient.getAttrList(skuId);
        if (baseAttrInfoList != null) {
            List<SearchAttr> searchAttrList = baseAttrInfoList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());

//                一个sku只对应一个属性值
                List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
                searchAttr.setAttrValue(baseAttrValueList.get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());

            goods.setAttrs(searchAttrList);
        }

//            查询SKU信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

//            查询品牌
        BaseTrademark baseTrademark = productFeignClient.getTrademark(skuInfo.getTmId());
        if (baseTrademark != null) {
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(baseTrademark.getTmName());
            goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        }

//            查询品牌分类
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if (baseCategoryView != null) {
            goods.setCategory1Id(baseCategoryView.getCategory1Id());
            goods.setCategory1Name(baseCategoryView.getCategory1Name());
            goods.setCategory2Id(baseCategoryView.getCategory2Id());
            goods.setCategory2Name(baseCategoryView.getCategory2Name());
            goods.setCategory3Id(baseCategoryView.getCategory3Id());
            goods.setCategory3Name(baseCategoryView.getCategory3Name());
        }

        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());

        this.goodsRepository.save(goods);

    }

    /**
     * 下架商品列表
     *
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * 更新热点
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
//        定义KEY
        String hotKey = "hotScore";

//        保存数据
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

//        每10次更新一次es
        if (hotScore % 10 == 0) {
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(hotScore));
            goodsRepository.save(goods);
        }

    }

    /**
     * 搜索列表
     *
     * @param searchParam
     * @return
     * @throws IOException
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
//        构建DSL语句
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(response);

//        制作返回结果集
        SearchResponseVo responseVO = this.parseSearchResult(response);
//        设置每页条数
        responseVO.setPageSize(searchParam.getPageSize());
//        设置当前页
        responseVO.setPageNo(searchParam.getPageNo());
//        设置总页数
        long totalPages = (responseVO.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();
        responseVO.setTotalPages(totalPages);

        return responseVO;
    }

    /**
     * 制作返回结果集
     *
     * @param response
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse response) {
//        声明对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();

//        品牌数据封装
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

//        获取品牌的聚合结果
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
//            获取品牌集合数据
            List<SearchResponseTmVo> responseTmVoList = buckets.stream().map(bucket -> {
//                创建品牌对象
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();

//                封装id
                long tmId = bucket.getKeyAsNumber().longValue();
                searchResponseTmVo.setTmId(tmId);
//                封装name
                Map<String, Aggregation> tmSubAggeregation = bucket.getAggregations().asMap();

//                获取品牌名称聚合对象
                ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmSubAggeregation.get("tmNameAgg");
                String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);

//                封装logoUrl
                ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmSubAggeregation.get("tmLogoUrlAgg");
                String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

                return searchResponseTmVo;
            }).collect(Collectors.toList());

//            设置响应信息到对象
            searchResponseVo.setTrademarkList(responseTmVoList);
        }

//        封装平台属性集合数据
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");

//        获取id子聚合
        Map<String, Aggregation> attrSubAggregation = attrAgg.getAggregations().asMap();
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrSubAggregation.get("attrIdAgg");

//        获取聚合数据
        List<? extends Terms.Bucket> subBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(subBuckets)) {
            List<SearchResponseAttrVo> searchResponseAttrVoList = subBuckets.stream().map(subBucket -> {
//                创建拼团封装对象
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

//                封装平台属性id
                long attrId = subBucket.getKeyAsNumber().longValue();
                searchResponseAttrVo.setAttrId(attrId);

//                获取子聚合数据
                Map<String, Aggregation> subsubAggergation = subBucket.getAggregations().asMap();

//                封装平台属性名
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subsubAggergation.get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);

//                封装平台属性值
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) subsubAggergation.get("attrValueAgg");

//                获取属性值的结果集
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueAggBuckets)) {
//                    获取属性值集合
                    List<String> attrValueList = attrValueAggBuckets.stream()
                            .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                            .collect(Collectors.toList());
                    searchResponseAttrVo.setAttrValueList(attrValueList);
                }

                return searchResponseAttrVo;
            }).collect(Collectors.toList());

//            设置到响应对象中
            searchResponseVo.setAttrsList(searchResponseAttrVoList);
        }

//        封装商品goods数据
        SearchHit[] hits = response.getHits().getHits();

//        定义集合接收goods数据
        List<Goods> goodsList = new ArrayList<>();

        if (hits != null && hits.length > 0) {
            for (SearchHit hit : hits) {
                Goods goods = JSONObject.parseObject(hit.getSourceAsString(), Goods.class);
//                获取高亮数据
                if (hit.getHighlightFields().get("title") != null) {
//                    获取高亮
                    HighlightField title = hit.getHighlightFields().get("title");
                    goods.setTitle(title.getFragments()[0].toString());
                }
                goodsList.add(goods);
            }
        }

//        设置商品集合数据
        searchResponseVo.setGoodsList(goodsList);

        return searchResponseVo;
    }

    /**
     * 构建DSL语句
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildQueryDsl(SearchParam searchParam) {
//        构建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

//        构建 boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

//        判断查询条件是否为空 关键字
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            MatchQueryBuilder title =
                    QueryBuilders.matchQuery("title", searchParam.getKeyword())
                            .operator(Operator.AND);
            boolQueryBuilder.must(title);
        }

//        构建品牌查询
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = StringUtils.split(trademark, ":");
            if (split != null && split.length == 2) {
//                根据品牌Id过滤
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

//        构建分类过滤 用户在点击的时候，只能点击一个值，所以此处使用term
        if (searchParam.getCategory1Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }

//        构建分类过滤
        if (searchParam.getCategory2Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }

//        构建分类过滤
        if (searchParam.getCategory3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }

//        构建平台属性查询
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = StringUtils.split(prop, ":");
                if (split != null && split.length == 3) {
//                    构建嵌套查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

//                    构建嵌套子查询
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();

//                    构建子查询
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));

                    boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));

//                    添加到整个过滤对象中
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

//        执行查询方法
        searchSourceBuilder.query(boolQueryBuilder);

//        构建分页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

//        排序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
//            判断排序规则
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
//                排序的字段
                String field = null;

//                数组中的第一个参数
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            } else {
//                没有传值的时候使用默认值
                searchSourceBuilder.sort("hotScore", SortOrder.DESC);
            }

        }

//        构建高亮查询
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");

        searchSourceBuilder.highlighter(highlightBuilder);

//        设置品牌聚合
        TermsAggregationBuilder termsAggregationBuilder =
                AggregationBuilders.terms("tmIdAgg").field("tmId")
                        .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                        .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

        searchSourceBuilder.aggregation(termsAggregationBuilder);

//        设置平台属性聚合
        searchSourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "attrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

//        结果集过滤
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price"}, null);

        SearchRequest searchRequest = new SearchRequest("goods");

        searchRequest.source(searchSourceBuilder);
        System.out.println("dsl:" + searchSourceBuilder.toString());
        return searchRequest;
    }
}
