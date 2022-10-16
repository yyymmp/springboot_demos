package com.hibernate.test;

import com.hibernate.entity.Customer;
import com.hibernate.entity.Order;
import com.hibernate.entity.People;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

/**
 * @author jlz
 * @date 2022年10月16日 15:06
 */
public class HQLTest {

    public static void main(String[] args) {
        Configuration configuration = new Configuration().configure("hibernate.xml");
        //获取sessionFacory
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        //hql不能执行新增 必须使用实体类名 不能使用people
        String hql = "from People";
        Query query = session.createQuery(hql);
        List<People> list = query.list();
        for (People o : list) {
            System.out.println(o);
        }
        //分页查询 使用query方法

        query.setFirstResult(1);
        query.setMaxResults(2);
        List<People> list1 = query.list();
        for (People o : list1) {
            System.out.println(o);
        }


        //where条件查询 与sql没有区别
        String hql2 = "from People where id = 1";
        Query query1 = session.createQuery(hql2);
        System.out.println(query1.list().get(0));


        //模糊查询
        String hql3 = "from People where name like '%1%'  order by id";
        Query query3 = session.createQuery(hql3);
        System.out.println(query3.list().get(0));

        //占位符
        String hql4 = "from People where name = :name";
        Query query4 = session.createQuery(hql4);
        query4.setParameter("name","jlz");
        List<People> list2 = query4.list();
        for (People people : list2) {
            System.out.println(people);
        }

        //级联查询
        String hql5 = "from Customer where name = :name";
        Query query5 = session.createQuery(hql5);
        query5.setParameter("name","jlz");
        Customer customer = (Customer)query5.uniqueResult();


        String hql6 = "from Order where customer = :customer";
        Query query6 = session.createQuery(hql6);
        query.setEntity("customer",customer);
        List<Order> list3 = query6.list();
        for (Order order : list3) {
            System.out.println(order);
        }

    }


}
