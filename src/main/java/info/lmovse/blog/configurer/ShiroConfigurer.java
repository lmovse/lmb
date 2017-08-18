package info.lmovse.blog.configurer;

import info.lmovse.blog.shiro.AuthRealm;
import info.lmovse.blog.shiro.MyCredentialsMatcher;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by lmovse on 2017/8/18.
 * Tomorrow is a nice day.
 */
@Configuration
public class ShiroConfigurer {
    private static Logger log = LoggerFactory.getLogger(ShiroConfigurer.class);

    @Bean
    public CacheManager cacheManager(EhCacheManagerFactoryBean factoryBean) {
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManager(factoryBean.getObject());
        return cacheManager;
    }

    @Bean
    public SecurityManager manager(EhCacheManagerFactoryBean factoryBean) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager(realm());
        manager.setCacheManager(cacheManager(factoryBean));
        return manager;
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator creator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor processor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor advisor(EhCacheManagerFactoryBean factoryBean) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(manager(factoryBean));
        return advisor;
    }

    @Bean
    public ShiroFilterFactoryBean factoryBean(EhCacheManagerFactoryBean factoryBean) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(manager(factoryBean));
        bean.setLoginUrl("/admin/login");
        Map<String, String> filterChainDefinition = new LinkedHashMap<>();
        filterChainDefinition.put("/**/css/**", "anon");
        filterChainDefinition.put("/**/js/**", "anon");
        filterChainDefinition.put("/**/images/**", "anon");
        filterChainDefinition.put("/**/img/**", "anon");
        filterChainDefinition.put("/**/plugins/**", "anon");
        filterChainDefinition.put("/**", "authc");
        filterChainDefinition.put("/*.*", "authc");
        bean.setFilterChainDefinitionMap(filterChainDefinition);
        return bean;
    }

    @Bean
    public Realm realm() {
        AuthRealm authRealm = new AuthRealm();
        authRealm.setCredentialsMatcher(matcher());

        // 开启认证缓存
        authRealm.setAuthenticationCachingEnabled(true);

        // 设置认证缓存的名称对应 ehcache 中配置
        authRealm.setAuthenticationCacheName("authenticationCache");

        // 开启授权缓存
        authRealm.setAuthorizationCachingEnabled(true);

        // 设置授权缓存的名称对应 ehcache 中配置
        authRealm.setAuthorizationCacheName("authorizationCache");
        return authRealm;
    }

    @Bean
    public CredentialsMatcher matcher() {
        return new MyCredentialsMatcher();
    }

}
