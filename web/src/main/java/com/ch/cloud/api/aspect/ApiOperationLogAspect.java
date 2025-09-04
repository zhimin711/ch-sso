package com.ch.cloud.api.aspect;

import com.ch.cloud.api.annotation.HasPermission;
import com.ch.cloud.api.service.IApiOperationLogService;
import com.ch.toolkit.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class ApiOperationLogAspect {

    @Autowired
    private IApiOperationLogService apiOperationLogService;

    @Autowired
    private HttpServletRequest request;

    @Pointcut(
            "(@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)) && "
                    + "(@annotation(org.springframework.web.bind.annotation.PostMapping) || "
                    + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
                    + "@annotation(org.springframework.web.bind.annotation.DeleteMapping)) && "
                    + "@annotation(com.ch.cloud.api.annotation.HasPermission)")
    public void operationLogPointcut() {
    }

    @AfterReturning(pointcut = "operationLogPointcut()", returning = "result")
    public void afterOperation(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String operationType = getOperationType(method);
        String module = joinPoint.getTarget().getClass().getSimpleName();
        String username = ContextUtil.getUsername();
        String userId = ContextUtil.getUserId();
        String ip = request.getRemoteAddr();

        // 从 Cookie 获取 projectId
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("projectId".equals(cookie.getName())) {
                    module = cookie.getValue();
                    break;
                }
            }
        }


        String operationContent = method.getName() + " " + java.util.Arrays.toString(joinPoint.getArgs());

        apiOperationLogService.saveLog(userId, username, module, operationType, operationContent, ip);
    }

    private String getOperationType(Method method) {
        HasPermission permission = method.getAnnotation(HasPermission.class);
        if (permission != null) {
            return permission.value();
        }
        if (method.isAnnotationPresent(PostMapping.class)) {
            return "新增";
        }
        if (method.isAnnotationPresent(PutMapping.class)) {
            return "修改";
        }
        if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "删除";
        }
        return "操作";
    }
}
