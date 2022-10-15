package com.hibernate.test;

import com.hibernate.entity.Account;
import com.hibernate.entity.Course;
import com.hibernate.entity.Customer;
import com.hibernate.entity.Order;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author jlz
 * @date 2022年10月13日 20:39
 */
public class Test3 {

    public static void main(String[] args) {
        /**
         * 一对多测试 Customer-Order
         */
        Configuration configuration = new Configuration().configure("hibernate.xml");
        //获取sessionFacory
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        Account account = new Account();
        account.setName("zhangsan");
        Course course = new Course();
        course.setName("物理");

        Set<Course> courseSet = new HashSet<>();
        courseSet.add(course);
        account.setCourses(courseSet);

        Set<Account> accountSet = new HashSet<>();
        accountSet.add(account);
        course.setAccounts(accountSet);

        session.save(account);
        session.save(course);
        session.beginTransaction().commit();
        session.close();
    }
}
