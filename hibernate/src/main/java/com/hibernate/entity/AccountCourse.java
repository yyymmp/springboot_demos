package com.hibernate.entity;

import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder.In;
import lombok.Data;

/**
 * @author jlz
 * @date 2022年10月13日 21:14
 */
@Data
public class AccountCourse {

    private Integer id;
    private Integer aid;
    private Integer cid;
}
