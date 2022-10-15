package com.hibernate.entity;

import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jlz
 * @date 2022年10月13日 21:14
 */
@Setter
@Getter
public class Account {

    private Integer id;
    private String name;
    private Set<Course> courses;

    @Override
    public String toString() {
        return id + "  " + name;
    }
}
