package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 角色：1-普通用户 2-作者 3-管理员
     */
    private Integer role;

    /**
     * 状态：0-禁用 1-正常
     */
    private Integer status;

    /**
     * 提供者：local/google/github
     */
    private String provider;

    /**
     * 提供者用户 ID
     */
    private String providerId;

    /**
     * 最后登录时间
     */
    private ZonedDateTime lastLoginAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private ZonedDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private ZonedDateTime updatedAt;
}
