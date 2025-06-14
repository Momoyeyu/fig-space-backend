package ai.momoyeyu.figspace.aop;

import ai.momoyeyu.figspace.constant.UserConstant;
import ai.momoyeyu.figspace.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 访问拦截器，验证JWT令牌
 * @author Momoyeyu
 */
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    /**
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return boolean 是否通过验证
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跳过 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(UserConstant.AUTHORIZATION_HEADER);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtils.getUserAccountFromToken(token);
            return jwtUtils.validateToken(token, username);
        }
        // 令牌缺失
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

}
