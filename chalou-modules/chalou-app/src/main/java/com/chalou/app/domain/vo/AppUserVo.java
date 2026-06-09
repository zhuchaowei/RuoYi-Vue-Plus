package com.chalou.app.domain.vo;

import com.chalou.app.domain.AppUser;
import com.chalou.common.translation.annotation.Translation;
import com.chalou.common.translation.constant.TransConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * app用户视图对象 app_user
 */
@Data
@AutoMapper(target = AppUser.class)
public class AppUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private Long avatar;

    /**
     * 密码
     */
    @JsonIgnore
    @JsonProperty
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    private String status;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 创建时间
     */
    private Date createTime;

}
