package com.heima.es;

import com.alibaba.fastjson.JSON;
import com.heima.es.mapper.ApArticleMapper;
import com.heima.es.pojo.SearchArticleVo;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApArticleTest {

    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private RestHighLevelClient highLevelClient;

    /**
     * 数据初始化到 es
     * @throws Exception
     */
    @Test
    public void init() throws Exception {
        List<SearchArticleVo> articleVos = apArticleMapper.loadArticle();
        if (CollectionUtils.isEmpty(articleVos)) {
            return;
        }
        // 构建 bulk 请求
        BulkRequest bulkRequest = new BulkRequest("app_info_article");
        for (SearchArticleVo articleVo : articleVos) {
            IndexRequest indexRequest = new IndexRequest().id(articleVo.getId() + "")
                    .source(JSON.toJSONString(articleVo), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
}
