package com.es.demo;

import java.io.IOException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jlz
 * @date 2022年09月26日 20:51
 */
public class RestHighLevelClientIndexTest extends DemoApplicationTests {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public RestHighLevelClientIndexTest(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 创建索引 创建映射
     */
    @Test
    public void testIndexAndMapping() throws IOException {
        //1 创建索引请求对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("products");
        //指定映射 参数1:指定映射json结构 2 指定数据类型
        //一般都是从kibana上创建好复制过来
        createIndexRequest.mapping("{\n"
                + "    \"properties\": {\n"
                + "      \"title\":{\n"
                + "        \"type\": \"keyword\"\n"
                + "      },\n"
                + "      \"price\":{\n"
                + "        \"type\": \"double\"\n"
                + "      },\n"
                + "      \"create_at\":{\n"
                + "        \"type\": \"date\"\n"
                + "      }\n"
                + "      ,\"description\":{\n"
                + "        \"type\": \"text\"\n"
                + "        , \"analyzer\": \"ik_max_word\"\n"
                + "      }\n"
                + "  }\n"
                + "}", XContentType.JSON);
        //2 请求配置对象(使用默认配置)
        RequestOptions aDefault = RequestOptions.DEFAULT;
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, aDefault);
        System.out.println(createIndexResponse);
        restHighLevelClient.close();
    }

    /**
     * 删除索引
     */
    @Test
    public void testDeleteIndex() throws IOException {
        AcknowledgedResponse products = restHighLevelClient.indices().delete(new DeleteIndexRequest("products"), RequestOptions.DEFAULT);
        System.out.println(products.isAcknowledged());
    }
}
