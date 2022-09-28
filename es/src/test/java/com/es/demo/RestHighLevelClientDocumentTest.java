package com.es.demo;

import java.io.IOException;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jlz
 * @date 2022年09月26日 21:21
 */
public class RestHighLevelClientDocumentTest extends DemoApplicationTests {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public RestHighLevelClientDocumentTest(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 更新一条文档
     */
    @Test
    public void testDoc() throws IOException {
        //更新的索引名 更新的id
        UpdateRequest updateRequest = new UpdateRequest("products", "2");
        updateRequest.doc("{\n"
                + "   \"title\":\"华莱士111\" \n"
                + "  }", XContentType.JSON);

        restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
    }

    /**
     * 索引一条文档
     */
    @Test
    public void testIndex() throws IOException {
        IndexRequest indexRequest = new IndexRequest("products");
        //id手动指定id  source放入json数据
        //indexRequest.id("2").source("{\n"
        //        + "  \"title\":\"华莱士\",\n"
        //        + "  \"price\":134.0,\n"
        //        + "  \"create_at\":\"1998-02-20\",\n"
        //        + "  \"description\":\"远近闻名这是饼干 \"\n"
        //        + "}\n", XContentType.JSON);
        indexRequest.source("{\n"
                + "  \"title\":\"瞎扯淡\",\n"
                + "  \"price\":134.0,\n"
                + "  \"create_at\":\"1998-02-20\",\n"
                + "  \"description\":\"你在扯什么淡 \"\n"
                + "}\n", XContentType.JSON);
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 删除一条文档
     */
    @Test
    public void testDelete() throws IOException {
        //更新的索引名 更新的id
        DeleteRequest updateRequest = new DeleteRequest("products", "2");
        DeleteResponse delete = restHighLevelClient.delete(updateRequest, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    /**
     * 基于id查询
     */
    @Test
    public void testSearch() throws IOException {
        //更新的索引名 更新的id
        GetRequest getRequest = new GetRequest("products", "2");
        GetResponse documentFields = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(documentFields.getSource());
    }

    /**
     * 查询所有
     */
    @Test
    public void testMatchAll() throws IOException {
        //指定索引
        SearchRequest searchRequest = new SearchRequest("products");
        //指定查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询所有
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        //关键词查询
        searchSourceBuilder.query(matchAllQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("条数: " + search.getHits().getTotalHits());
        for (SearchHit hit : search.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
        }
    }

    /**
     * 条件查询
     *
     * @throws IOException
     */
    @Test
    public void testQuery(QueryBuilder queryBuilder) throws IOException {
        //指定索引
        SearchRequest searchRequest = new SearchRequest("products");
        //关键词查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("条数: " + search.getHits().getTotalHits());
        for (SearchHit hit : search.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
        }
    }

    /**
     * 关键字查询
     *
     * @throws IOException
     */
    @Test
    public void testTerm() throws IOException {
        //term查询
        //TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("description", "什么");
        //range查询
        //RangeQueryBuilder price = QueryBuilders.rangeQuery("price").gt(0).lt(10);
        //前缀查询
        //PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery("description", "你");
        //通配符查询
        //WildcardQueryBuilder title = QueryBuilders.wildcardQuery("title", "华*");
        //ids查询
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery().addIds("6SQCeoMBLEJFg_iJyfPY");
        //多字段查询(常用)
        QueryBuilders.multiMatchQuery("华远近闻名").field("title").field("description");
        testQuery(idsQueryBuilder);
    }

    /**
     * 分页查询
     *
     * @throws IOException
     */
    @Test
    public void testPage() throws IOException {
        SearchRequest searchRequest = new SearchRequest("products");
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false).field("description").field("title").preTags("<span style='color:red;'>").postTags("</span>");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //sourceBuilder.query(QueryBuilders.matchAllQuery())
        sourceBuilder.query(QueryBuilders.termQuery("description", "远近闻名"))
                //分页
                .from(0)
                .size(2)
                //排序
                .sort("price", SortOrder.DESC)
                //指定字段返回
                //.fetchSource("","create_at")
                //高亮
                .highlighter(highlightBuilder);

        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("条数: " + search.getHits().getTotalHits());
        for (SearchHit hit : search.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
            //获取高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println(highlightFields);
        }
    }

    /**
     * 过滤查询  大量数据中筛选  不会计算得分 性能高于query
     */
    @Test
    public void testFilter() throws IOException {
        SearchRequest searchRequest = new SearchRequest("products");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery())
                        //使用postFilter进行过滤条件  也是一个QueryBuilders 所有所有的查询都可用
                        //.postFilter(QueryBuilders.termQuery("description","淡"));

                        .postFilter(QueryBuilders.idsQuery().addIds("1"));
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("条数: " + search.getHits().getTotalHits());
        for (SearchHit hit : search.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
            //获取高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println(highlightFields);
        }
    }

}
