package com.hibernate.test;

import com.hibernate.entity.Account;
import com.hibernate.entity.Course;
import com.hibernate.entity.Customer;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author jlz
 * @date 2022年10月13日 20:39
 */
public class Test4 {
    //延迟加载
    public static void main(String[] args) {
        /**
         * 一对多测试 Customer-Order
         */
        Configuration configuration = new Configuration().configure("hibernate.xml");
        //获取sessionFacory
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        Customer customer = session.get(Customer.class, 1);
        System.out.println(customer);
        session.close();
    }
}
