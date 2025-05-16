package com.heima.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ApArticleSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ApArticleServiceImpl implements ApArticleSearchService {

    @Autowired
    private RestHighLevelClient highLevelClient;

    @Override
    public ResponseResult search(UserSearchDto searchDto) {

        try {
            SearchRequest request = new SearchRequest("app_info_article");

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            if (StringUtils.isEmpty(searchDto.getSearchWords())) {
                boolQueryBuilder.must(QueryBuilders.matchAllQuery());
            } else {
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(searchDto.getSearchWords())
                        .field("title")
                        .field("content"));
            }

            if (searchDto.getMinBehotTime() != null) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("publishTime").lt(searchDto.getMinBehotTime()));
            }

            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<font style='color:blue; font-size:inherit'>");
            highlightBuilder.postTags("</font>");
            sourceBuilder.highlighter(highlightBuilder);

            sourceBuilder.query(boolQueryBuilder);
            sourceBuilder.from(0).size(searchDto.getPageSize());
            request.source(sourceBuilder);

            SearchResponse search = highLevelClient.search(request, RequestOptions.DEFAULT);

            SearchHit[] hits = search.getHits().getHits();
            List<Map> result = new ArrayList<>();
            for (SearchHit hit : hits) {
                Map map = JSON.parseObject(hit.getSourceAsString(), Map.class);

                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (CollectionUtils.isEmpty(highlightFields)) {
                    map.put("h_title", map.get("title"));
                } else{
                    HighlightField highlightField = highlightFields.get("title");
                    Text[] fragments = highlightField.getFragments();
                    map.put("h_title", StringUtils.join(fragments));
                }

                result.add(map);
            }

            return ResponseResult.okResult(result);
        } catch (Exception e) {
            log.error("查询异常", e);
        }

        return ResponseResult.okResult(null);
    }
}
