package com.chalou.app.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * app密码登录对象
 */
@Data
public class AppLoginBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端id
     */
    @NotBlank(message = "{auth.clientid.not.blank}")
    private String clientId;

    /**
     * 租户ID
     */
    @NotBlank(message = "{tenant.number.not.blank}")
    private String tenantId;

    /**
     * 用户名/手机号
     */
    @NotBlank(message = "{user.username.not.blank}")
    @Length(min = 2, max = 30, message = "{user.username.length.valid}")
    private String username;

    /**
     * 用户密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, message = "{user.password.length.valid}")
    private String password;

}
