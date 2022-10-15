package com.hibernate.test;

import com.hibernate.entity.Customer;
import com.hibernate.entity.Order;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author jlz
 * @date 2022年10月13日 20:39
 */
public class Test2 {

    public static void main(String[] args) {
        /**
         * 一对多测试 Customer-Order
         */
        Configuration configuration = new Configuration().configure("hibernate.xml");
        //获取sessionFacory
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        Customer customer = new Customer();
        customer.setName("jlz");

        Order order = new Order();
        order.setName("dingdan");
        //建立关联关系
        order.setCustomer(customer);

        session.save(customer);
        session.save(order);
        session.beginTransaction().commit();
        session.close();
    }
}
