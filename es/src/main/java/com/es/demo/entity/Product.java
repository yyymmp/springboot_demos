package com.es.demo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author jlz
 * @date 2022年09月25日 23:52 Document :将这个类对象转化为es中一条文档进行录入 indexName: 索引名称 createIndex:索引不存在时创建索引
 */
@Data
@Document(indexName = "products", createIndex = true)
public class Product {

    /**
     * ID: 对象id值与文档_id进行映射
     */
    @Id
    private Integer id;
    @Field(type = FieldType.Keyword)
    private String title;
    @Field(type = FieldType.Float)
    private Double price;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;
}
