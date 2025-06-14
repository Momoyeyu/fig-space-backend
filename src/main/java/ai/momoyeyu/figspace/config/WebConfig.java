package ai.momoyeyu.figspace.config;

import ai.momoyeyu.figspace.aop.LoginCheckInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private LoginCheckInterceptor loginCheckInterceptor;

    /**
     * 限制访问（除了登录和注册界面）
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/api/**") // 拦截所有请求
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/doc.html#/**",
                        "/api/swagger-ui/**"); // 排除登录和注册接口
    }
}
