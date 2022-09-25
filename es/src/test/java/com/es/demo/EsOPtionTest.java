package com.es.demo;

import com.es.demo.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * @author jlz
 * @date 2022年09月25日 23:58
 */
public class EsOPtionTest extends DemoApplicationTests{


    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public EsOPtionTest(ElasticsearchOperations elasticsearchOperations){
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     *
     */
    @Test
    public void testIndex(){
        Product product = new Product();
        product.setId(1);
        product.setTitle("小浣熊干吃面");
        product.setPrice(1.5);
        product.setDescription("小浣熊干吃面真好吃");

        elasticsearchOperations.save(product);
    }
}
