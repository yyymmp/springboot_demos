package com.hibernate.entity;

import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * @author jlz
 * @date 2022年10月13日 21:14
 */
@Data
public class Course {
    private Integer id;
    private String  name;
    private Set<Account> accounts;
}
