package com.chalou.app.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * app用户注册对象
 */
@Data
public class AppRegisterBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    @NotBlank(message = "{tenant.number.not.blank}")
    private String tenantId;

    /**
     * 用户账号
     */
    @NotBlank(message = "{user.username.not.blank}")
    @Length(min = 2, max = 30, message = "{user.username.length.valid}")
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, message = "{user.password.length.valid}")
    private String password;

}
