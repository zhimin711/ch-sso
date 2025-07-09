package com.ch.cloud.api.aspect;

import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.domain.ApiPermission;
import com.ch.cloud.api.dto.ApiProjectRoleDTO;
import com.ch.cloud.api.service.IApiPermissionService;
import com.ch.cloud.api.service.IApiProjectService;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.cloud.upms.enums.RoleType;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 权限校验切面
 * 基于@HasPermission注解进行权限校验
 */
@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private IApiPermissionService apiPermissionService;

    @Autowired
    private IApiProjectService apiProjectService;

    @Autowired
    private UpmsUserClient upmsUserClient;

    /**
     * 环绕通知，拦截带有@HasPermission注解的方法
     */
    @Around("@annotation(com.ch.cloud.api.annotation.HasPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        HasPermission hasPermission = signature.getMethod().getAnnotation(HasPermission.class);
        
        if (hasPermission == null) {
            return joinPoint.proceed();
        }

        // 获取权限代码
        String permissionCode = hasPermission.value();
        
        // 从Cookie获取角色代码
        String roleCode = getValueFromCookie("roleCode");
        
        Assert.notEmpty(roleCode, PubError.NOT_EXISTS, "未获取到角色信息");

        // 校验权限
        Assert.isTrue(hasPermission(permissionCode, roleCode), PubError.NOT_ALLOWED, "此操作",permissionCode);
        String projectIdStr = getValueFromCookie("projectId");

        Assert.isTrue(CommonUtils.isNumeric(projectIdStr), PubError.NOT_EXISTS, "未获取到项目信息");
        Long projectId = Long.parseLong(projectIdStr);
        ApiProjectRoleDTO roleDTO = apiProjectService.findByUserIdAndProjectId(ContextUtil.getUserId(), projectId);
        if (roleDTO == null || !CommonUtils.isEquals(roleDTO.getRole(),roleCode)) {
            List<RoleType> projectRoles = upmsUserClient.listProjectRoles(ContextUtil.getUserId(), projectId, roleCode);
            Assert.notEmpty(projectRoles, PubError.NOT_ALLOWED, roleCode);
        }
        Assert.isFalse(RoleType.isVisitor(roleCode) && roleDTO == null, PubError.NOT_ALLOWED, roleCode, "未授权访问");


        // 权限校验通过，继续执行原方法
        return joinPoint.proceed();
    }

    /**
     * 从Cookie中获取角色代码
     */
    private String getValueFromCookie(String cookieName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // 根据实际Cookie名称调整
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }

    /**
     * 校验用户是否拥有指定权限
     * @param permissionCode 权限代码
     * @param roleCode 角色代码
     * @return true-有权限，false-无权限
     */
    private boolean hasPermission(String permissionCode, String roleCode) {
        if (CommonUtils.isEmpty(permissionCode) || CommonUtils.isEmpty(roleCode)) {
            return false;
        }

        // 查询权限配置
        List<ApiPermission> permissions = apiPermissionService.lambdaQuery()
                .eq(ApiPermission::getCode, permissionCode)
                .list();

        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        // 检查角色是否在权限的roles中
        for (ApiPermission permission : permissions) {
            String roles = permission.getRoles();
            if (CommonUtils.isNotEmpty(roles) && roles.contains(roleCode)) {
                return true;
            }
        }

        return false;
    }
}
