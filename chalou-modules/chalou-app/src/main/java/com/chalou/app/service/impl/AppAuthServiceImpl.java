package com.chalou.app.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chalou.app.domain.AppUser;
import com.chalou.app.domain.bo.AppLoginBody;
import com.chalou.app.domain.bo.AppRegisterBody;
import com.chalou.app.domain.vo.AppLoginVo;
import com.chalou.app.domain.vo.AppUserVo;
import com.chalou.app.mapper.AppUserMapper;
import com.chalou.app.service.IAppAuthService;
import com.chalou.common.core.constant.CacheConstants;
import com.chalou.common.core.constant.SystemConstants;
import com.chalou.common.core.constant.TenantConstants;
import com.chalou.common.core.domain.model.LoginUser;
import com.chalou.common.core.enums.UserType;
import com.chalou.common.core.exception.ServiceException;
import com.chalou.common.core.exception.user.UserException;
import com.chalou.common.core.utils.DateUtils;
import com.chalou.common.core.utils.MessageUtils;
import com.chalou.common.core.utils.ServletUtils;
import com.chalou.common.core.utils.StringUtils;
import com.chalou.common.redis.utils.RedisUtils;
import com.chalou.common.satoken.utils.LoginHelper;
import com.chalou.common.tenant.exception.TenantException;
import com.chalou.common.tenant.helper.TenantHelper;
import com.chalou.system.domain.vo.SysClientVo;
import com.chalou.system.domain.vo.SysTenantVo;
import com.chalou.system.service.ISysClientService;
import com.chalou.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;

/**
 * app认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements IAppAuthService {

    @Value("${user.password.maxRetryCount}")
    private Integer maxRetryCount;

    @Value("${user.password.lockTime}")
    private Integer lockTime;

    private final AppUserMapper appUserMapper;
    private final ISysClientService clientService;
    private final ISysTenantService tenantService;

    @Override
    public AppLoginVo login(AppLoginBody body) {
        String tenantId = body.getTenantId();
        SysClientVo client = clientService.queryByClientId(body.getClientId());
        if (ObjectUtil.isNull(client) || !StringUtils.equals(client.getClientKey(), "app")) {
            throw new ServiceException("app客户端不存在或类型不正确");
        } else if (!SystemConstants.NORMAL.equals(client.getStatus())) {
            throw new ServiceException(MessageUtils.message("auth.grant.type.blocked"));
        }

        checkTenant(tenantId);
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            AppUserVo user = loadUserByUsername(tenantId, body.getUsername());
            checkLogin(tenantId, body.getUsername(), () -> !BCrypt.checkpw(body.getPassword(), user.getPassword()));
            return buildLoginUser(user);
        });
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());

        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(client.getDeviceType());
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        LoginHelper.login(loginUser, model);

        recordLoginInfo(loginUser.getUserId(), ServletUtils.getClientIP());

        AppLoginVo loginVo = new AppLoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        return loginVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(AppRegisterBody body) {
        String tenantId = body.getTenantId();
        checkTenant(tenantId);
        TenantHelper.dynamic(tenantId, () -> {
            if (ObjectUtil.isNotNull(loadUserByUsernameSilently(tenantId, body.getUsername()))) {
                throw new ServiceException("用户账号已存在");
            }
            if (StringUtils.isNotBlank(body.getPhonenumber()) && ObjectUtil.isNotNull(loadUserByPhone(tenantId, body.getPhonenumber()))) {
                throw new ServiceException("手机号码已存在");
            }
            AppUser user = new AppUser();
            user.setTenantId(tenantId);
            user.setUserName(body.getUsername());
            user.setNickName(StringUtils.blankToDefault(body.getNickname(), body.getUsername()));
            user.setPhonenumber(body.getPhonenumber());
            user.setPassword(BCrypt.hashpw(body.getPassword()));
            user.setStatus(SystemConstants.NORMAL);
            user.setDelFlag(SystemConstants.NORMAL);
            appUserMapper.insert(user);
            return null;
        });
    }

    @Override
    public void logout() {
        try {
            StpUtil.logout();
        } catch (NotLoginException ignored) {
        }
    }

    @Override
    public AppUserVo getLoginUser() {
        Long userId = LoginHelper.getUserId();
        AppUserVo user = appUserMapper.selectVoById(userId);
        if (ObjectUtil.isNull(user)) {
            throw new UserException("user.not.exists", userId);
        }
        return user;
    }

    private LoginUser buildLoginUser(AppUserVo user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setTenantId(user.getTenantId());
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUserName());
        loginUser.setNickname(user.getNickName());
        loginUser.setUserType(UserType.APP_USER.getUserType());
        loginUser.setMenuPermission(CollUtil.newHashSet());
        loginUser.setRolePermission(CollUtil.newHashSet());
        return loginUser;
    }

    private AppUserVo loadUserByUsername(String tenantId, String username) {
        AppUserVo user = loadUserByUsernameSilently(tenantId, username);
        if (ObjectUtil.isNull(user)) {
            user = loadUserByPhone(tenantId, username);
        }
        if (ObjectUtil.isNull(user)) {
            log.info("app登录租户：{} 用户：{} 不存在.", tenantId, username);
            throw new UserException("user.not.exists", username);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("app登录租户：{} 用户：{} 已被停用.", tenantId, username);
            throw new UserException("user.blocked", username);
        }
        return user;
    }

    private AppUserVo loadUserByUsernameSilently(String tenantId, String username) {
        return appUserMapper.selectVoOne(new LambdaQueryWrapper<AppUser>()
            .eq(AppUser::getTenantId, tenantId)
            .eq(AppUser::getUserName, username));
    }

    private AppUserVo loadUserByPhone(String tenantId, String phonenumber) {
        return appUserMapper.selectVoOne(new LambdaQueryWrapper<AppUser>()
            .eq(AppUser::getTenantId, tenantId)
            .eq(AppUser::getPhonenumber, phonenumber));
    }

    private void checkLogin(String tenantId, String username, java.util.function.Supplier<Boolean> supplier) {
        String errorKey = CacheConstants.PWD_ERR_CNT_KEY + UserType.APP_USER.getUserType() + ":" + tenantId + ":" + username;
        int errorNumber = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(errorKey), 0);
        if (errorNumber >= maxRetryCount) {
            throw new UserException("user.password.retry.limit.exceed", maxRetryCount, lockTime);
        }
        if (supplier.get()) {
            errorNumber++;
            RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
            if (errorNumber >= maxRetryCount) {
                throw new UserException("user.password.retry.limit.exceed", maxRetryCount, lockTime);
            }
            throw new UserException("user.password.retry.limit.count", errorNumber);
        }
        RedisUtils.deleteObject(errorKey);
    }

    private void checkTenant(String tenantId) {
        if (!TenantHelper.isEnable()) {
            return;
        }
        if (StringUtils.isBlank(tenantId)) {
            throw new TenantException("tenant.number.not.blank");
        }
        if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
            return;
        }
        SysTenantVo tenant = tenantService.queryByTenantId(tenantId);
        if (ObjectUtil.isNull(tenant)) {
            throw new TenantException("tenant.not.exists");
        } else if (SystemConstants.DISABLE.equals(tenant.getStatus())) {
            throw new TenantException("tenant.blocked");
        } else if (ObjectUtil.isNotNull(tenant.getExpireTime()) && new Date().after(tenant.getExpireTime())) {
            throw new TenantException("tenant.expired");
        }
    }

    private void recordLoginInfo(Long userId, String ip) {
        AppUser user = new AppUser();
        user.setUserId(userId);
        user.setLoginIp(ip);
        user.setLoginDate(DateUtils.getNowDate());
        appUserMapper.updateById(user);
    }

}
