package com.es.demo;

import com.es.demo.entity.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jlz
 * @date 2022年09月26日 20:51
 */
public class RestHighLevelClientObjTest extends DemoApplicationTests {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public RestHighLevelClientObjTest(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 对象放入es中 前面的restHighLevelClient应用都是通过json字符串插入es
     */
    @Test
    public void testIndex() throws IOException {
        Product product = new Product();
        product.setId(1);
        product.setTitle("小碗换真好吃");
        product.setPrice(12.2);
        product.setDescription("小碗换真好吃");

        IndexRequest indexRequest = new IndexRequest("products");
        //序列化插入
        indexRequest.id(product.getId().toString()).source(new ObjectMapper().writeValueAsString(product), XContentType.JSON);
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 取出后得到解析为对象
     */
    @Test
    public void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("products");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());

        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Product> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            //直接将字符串转成
            Product product = new ObjectMapper().readValue(hit.getSourceAsString(), Product.class);
            System.out.println(product);
        }
    }
}

