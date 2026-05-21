/**用户实体类，对应数据库user表*
 @author lzy
 */

package com.yatidle.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String phone;

    private String avatar;

    private String nickname;

    private String bio;

    private String campus;

    private Integer creditScore = 100;

    private Integer role = 0;

    private String status = "active";

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
