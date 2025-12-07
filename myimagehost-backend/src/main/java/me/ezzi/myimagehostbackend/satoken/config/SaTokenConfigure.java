package me.ezzi.myimagehostbackend.satoken.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import me.ezzi.myimagehostbackend.common.constant.RoleConstant;
import me.ezzi.myimagehostbackend.common.context.BaseContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {

            //获取当前用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            BaseContext.setCurrentId(userId);

            String requestPath = SaHolder.getRequest().getRequestPath();
            if (requestPath.contains("/admin/")) {
                StpUtil.checkRole(RoleConstant.ADMIN);
            }

        }))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**"
                );
    }
}
