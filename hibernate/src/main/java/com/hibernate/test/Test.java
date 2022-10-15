package com.hibernate.test;

import com.hibernate.entity.People;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author jlz
 * @date 2022年10月12日 21:33
 */
public class Test {

    public static void main(String[] args) {
        Configuration configuration = new Configuration().configure("hibernate.xml");
        //获取sessionFacory
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        People people = new People();
        people.setMoney(100.0);
        people.setName("jlz");
        session.save(people);
        session.beginTransaction().commit();
        session.close();
        System.out.println(configuration);
    }
}
