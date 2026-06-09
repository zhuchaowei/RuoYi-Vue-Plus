package com.chalou.app.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * app登录令牌信息
 */
@Data
public class AppLoginVo {

    /**
     * 授权令牌
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 授权令牌有效期
     */
    @JsonProperty("expire_in")
    private Long expireIn;

    /**
     * 应用id
     */
    @JsonProperty("client_id")
    private String clientId;

}
