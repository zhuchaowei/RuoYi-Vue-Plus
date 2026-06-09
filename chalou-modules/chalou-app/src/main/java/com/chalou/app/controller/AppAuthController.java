package com.chalou.app.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.chalou.app.domain.bo.AppLoginBody;
import com.chalou.app.domain.bo.AppRegisterBody;
import com.chalou.app.domain.vo.AppLoginVo;
import com.chalou.app.domain.vo.AppUserVo;
import com.chalou.app.service.IAppAuthService;
import com.chalou.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * app端认证接口
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/auth")
public class AppAuthController {

    private final IAppAuthService appAuthService;

    /**
     * app端登录
     */
    @SaIgnore
    @PostMapping("/login")
    public R<AppLoginVo> login(@Validated @RequestBody AppLoginBody body) {
        return R.ok(appAuthService.login(body));
    }

    /**
     * app端注册
     */
    @SaIgnore
    @PostMapping("/register")
    public R<Void> register(@Validated @RequestBody AppRegisterBody body) {
        appAuthService.register(body);
        return R.ok();
    }

    /**
     * 当前app用户信息
     */
    @GetMapping("/me")
    public R<AppUserVo> me() {
        return R.ok(appAuthService.getLoginUser());
    }

    /**
     * app端退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        appAuthService.logout();
        return R.ok("退出成功");
    }

}
