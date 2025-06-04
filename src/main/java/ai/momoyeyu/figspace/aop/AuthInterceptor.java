package ai.momoyeyu.figspace.aop;

import ai.momoyeyu.figspace.annotation.AuthCheck;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.enums.UserRoleEnum;
import ai.momoyeyu.figspace.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springdoc.core.service.RequestBodyService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private RequestBodyService requestBodyBuilder;

    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint pjp, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 1. 不需要权限，直接放行
        if (mustRoleEnum == null) {
            return pjp.proceed();
        }

        // 2. 获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        // 3. 没有权限，直接拒绝
        ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NO_AUTH_ERROR);

        // 4. 鉴定管理员权限
        if (mustRoleEnum == UserRoleEnum.ADMIN) {
            ThrowUtils.throwIf(!userRoleEnum.equals(UserRoleEnum.ADMIN), ErrorCode.NO_AUTH_ERROR);
        }
        // 5. 通过校验，放行
        return pjp.proceed();
    }

}
