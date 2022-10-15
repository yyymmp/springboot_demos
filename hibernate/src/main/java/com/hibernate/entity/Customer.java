package com.hibernate.entity;

import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jlz
 * @date 2022年10月11日 23:08
 */
@Setter
@Getter
public class Customer {
    private Integer id;
    private String  name;
    private Set<Order> orders;

    @Override
    public String toString() {
        return id + "  " + name;
    }
}
