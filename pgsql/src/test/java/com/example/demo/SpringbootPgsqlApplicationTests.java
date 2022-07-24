package com.example.demo;

import com.example.demo.mapper.UserMapper;
import com.example.demo.po.UserPO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootPgsqlApplicationTests {

    @Autowired
    UserMapper userMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void add() {
        UserPO userPO = new UserPO();
        userPO.setUsername("布克");
        userPO.setPwd("123456");
        int insert = userMapper.insert(userPO);
        System.out.println(insert);
    }

    @Test
    void get() {
        UserPO userPO = userMapper.selectById(1);
        System.out.println(userPO);
    }

    public static void main(String[] args) {
        System.out.println();
    }
}
