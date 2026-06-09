package com.chalou.app.service;

import com.chalou.app.domain.bo.AppLoginBody;
import com.chalou.app.domain.bo.AppRegisterBody;
import com.chalou.app.domain.vo.AppLoginVo;
import com.chalou.app.domain.vo.AppUserVo;

/**
 * app认证服务
 */
public interface IAppAuthService {

    /**
     * app端密码登录
     *
     * @param body 登录信息
     * @return token信息
     */
    AppLoginVo login(AppLoginBody body);

    /**
     * app端注册
     *
     * @param body 注册信息
     */
    void register(AppRegisterBody body);

    /**
     * app端退出登录
     */
    void logout();

    /**
     * 获取当前app用户
     *
     * @return app用户信息
     */
    AppUserVo getLoginUser();

}
