package com.hibernate.entity;

import lombok.Data;

/**
 * @author jlz
 * @date 2022年10月11日 23:07
 */
@Data
public class Order {
    private Integer id;
    private String name;
    private Customer customer;
    @Override
    public String toString() {
        return id + "  " + name;
    }
}
