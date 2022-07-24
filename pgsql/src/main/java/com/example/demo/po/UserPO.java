package com.example.demo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jlz
 * @date 2022年07月24日 17:18
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
@TableName("t_user")
public class UserPO {
    @TableId(type = IdType.AUTO)
    private String id;

    private String username;

    private String pwd;
}
