package com.es.demo;

import co.elastic.clients.elasticsearch._types.aggregations.AggregateBuilders;
import java.io.IOException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedDoubleTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jlz
 * @date 2022年09月28日 20:19
 */
public class RestHighLevelClientAggsTest extends DemoApplicationTests {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public RestHighLevelClientAggsTest(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 分组聚合
     */
    @Test
    public void testAggs() throws IOException {
        SearchRequest searchRequest = new SearchRequest("fruit");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //设置聚合 对查询所有进行聚合
        sourceBuilder.query(QueryBuilders.matchAllQuery())
                //通过price group
                .aggregation(AggregationBuilders.terms("price_group").field("price")).size(0);
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //  "aggregations" : {
        //    "price_group" : {
        //      "doc_count_error_upper_bound" : 0,
        //      "sum_other_doc_count" : 0,
        //      "buckets" : [
        //        {
        //          "key" : 29.0,
        //          "doc_count" : 2
        //        },
        //        {
        //          "key" : 9.9,
        //          "doc_count" : 1
        //        }
        //      ]
        //    }
        //  }
        Aggregations aggregations = search.getAggregations();
        //key=value 形式转化为ParsedDoubleTerms
        ParsedDoubleTerms aggregation = aggregations.get("price_group");
        for (Bucket bucket : aggregation.getBuckets()) {
            System.out.println(bucket.getKey() + "<==>" + bucket.getDocCount());
        }
    }

    /**
     * max => ParsedAvg min => Parsedmin avg => ParsedAvg
     * @throws IOException
     */
    @Test
    public void testAggsFun() throws IOException {

        SearchRequest searchRequest = new SearchRequest("fruits");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(QueryBuilders.matchAllQuery())
                //对price求和
                .aggregation(AggregationBuilders.sum("sum").field("price")).size(0);
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        ParsedSum sum = search.getAggregations().get("sum");
        System.out.println(sum.getValue());
    }
}
