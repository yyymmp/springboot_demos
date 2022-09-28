package com.es.demo;

import com.es.demo.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

/**
 * @author jlz
 * @date 2022年09月25日 23:58
 */
public class EsOPtionTest extends DemoApplicationTests {


    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public EsOPtionTest(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * 索引或更新一条文档
     */
    @Test
    public void testIndex() {
        Product product = new Product();
        product.setId(2);
        product.setTitle("小浣熊干吃面");
        product.setPrice(4.5);
        product.setDescription("真好吃");

        //id存在时更新 不存在时新增
        elasticsearchOperations.save(product);
    }

    /**
     * 通过id查询
     */
    @Test
    public void testSearch() {
        //id存在时更新 不存在时新增
        Product product = elasticsearchOperations.get("1", Product.class);
        System.out.println(product);
    }


    /**
     * 查询全部
     */
    @Test
    public void testSearchAll() {
        //id存在时更新 不存在时新增
        SearchHits<Product> search = elasticsearchOperations.search(Query.findAll(), Product.class);
        System.out.println("总分数"+search.getMaxScore());
        System.out.println("条数"+search.getTotalHits());

        for (SearchHit<Product> productSearchHit : search) {
            Product content = productSearchHit.getContent();
            System.out.println(content);
        }
    }

    /**
     * 根据id删除
     */
    @Test
    public void testDelete() {
        //id存在时更新 不存在时新增
        Product product = new Product();
        product.setId(1);
        elasticsearchOperations.delete(product);
    }

    /**
     * 删除全部
     */
    @Test
    public void testDeleteAll() {
        elasticsearchOperations.delete(Query.findAll(),Product.class);
    }
}
