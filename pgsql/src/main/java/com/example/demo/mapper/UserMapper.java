package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.po.UserPO;
import java.util.List;
import javax.jws.soap.SOAPBinding.Use;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jlz
 * @date 2022年07月24日 17:19
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO>{

}

