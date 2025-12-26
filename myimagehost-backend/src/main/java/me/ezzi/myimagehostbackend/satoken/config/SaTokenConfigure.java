package me.ezzi.myimagehostbackend.satoken.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
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

                    SaRouter.match("/admin/**", () -> {
                        StpUtil.checkRole(RoleConstant.ADMIN);
                    });

                    SaRouter.match("/**", () -> {
                        StpUtil.checkLogin();
                        BaseContext.setCurrentId(StpUtil.getLoginIdAsLong());
                    });

                }))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/forgetPassword"
                );

    }
}
