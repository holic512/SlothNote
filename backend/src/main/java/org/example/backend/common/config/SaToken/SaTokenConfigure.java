/**
 * File Name: SaTokenConfigure.java
 * Description: SaToken的配置类 其中包含对web的配置
 * Author: holic512
 * Created Date: 2024-09-17
 * Version: 1.0
 * Usage:
 * 操作mvc之前的路由过滤器 以及 访问前置函数的配置(跨域配置)
 */
package org.example.backend.common.config.SaToken;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import org.example.backend.common.util.StpKit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {


            // // 设置跨域响应头
            // SaHolder.getResponse()
            //         // 允许指定域访问跨域资源
            //         .setHeader("Access-Control-Allow-Origin", "*")
            //         // 允许所有请求方式
            //         .setHeader("Access-Control-Allow-Methods", "*")
            //         // 允许的header参数
            //         .setHeader("Access-Control-Allow-Headers", "*")
            //         // 有效时间
            //         .setHeader("Access-Control-Max-Age", "3600");


            // 路由拦截器
            SaRouter
                    // .match(SaHttpMethod.OPTIONS)
                    // .free(r -> System.out.println("--------OPTIONS预检请求，不做处理"))
                    // .back()

                    // 排除拦截接口
                    .notMatch("/auth/**")
                    .notMatch("/admin/auth/**")
                    .notMatch("/user/auth/**")
                    .notMatch("/images/**")

                    .match("/admin/**").check(r -> StpKit.ADMIN.checkLogin())
                    .match("/user/**").check(r -> StpKit.USER.checkLogin());




            // 这个位置会出现bug 莫名其妙
            /**
             *                             _ooOoo_
             *                            o8888888o
             *                            88" . "88
             *                            (| -_- |)
             *                            O\  =  /O
             *                         ____/`---'\____
             *                       .'  \\|     |//  `.
             *                      /  \\|||  :  |||//  \
             *                     /  _||||| -:- |||||-  \
             *                     |   | \\\  -  /// |   |
             *                     | \_|  ''\---/''  |   |
             *                     \  .-\__  `-`  ___/-. /
             *                   ___`. .'  /--.--\  `. . __
             *                ."" '<  `.___\_<|>_/___.'  >'"".
             *               | | :  `- \`.;`\ _ /`;.`/ - ` : | |
             *               \  \ `-.   \_ __\ /__ _/   .-` /  /
             *          ======`-.____`-.___\_____/___.-`____.-'======
             *                             `=---='
             *          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
             *                     佛祖保佑        永无BUG
             *            佛曰:
             *                   写字楼里写字间，写字间里程序员；
             *                   程序人员写程序，又拿程序换酒钱。
             *                   酒醒只在网上坐，酒醉还来网下眠；
             *                   酒醉酒醒日复日，网上网下年复年。
             *                   但愿老死电脑间，不愿鞠躬老板前；
             *                   奔驰宝马贵者趣，公交自行程序员。
             *                   别人笑我忒疯癫，我笑自己命太贱；
             *                   不见满街漂亮妹，哪个归得程序员？
             */

        }


        )).addPathPatterns("/**");

    }

    /**
     * 注册 [Sa-Token 全局过滤器]
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()

                // 指定 [拦截路由] 与 [放行路由]
                .addExclude("/**").addExclude("/favicon.ico")

                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    SaHolder.getResponse()

                            // ---------- 设置跨域响应头 ----------
                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "*")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600")
                    ;

                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match(SaHttpMethod.OPTIONS)
                            // .free(r -> System.out.println("--------OPTIONS预检请求，不做处理"))
                            .back();
                });
    }



}
