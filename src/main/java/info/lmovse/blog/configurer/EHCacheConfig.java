package info.lmovse.blog.configurer;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by lmovse on 2017/8/18.
 * Tomorrow is a nice day.
 */
@Configuration
@EnableCaching
public class EHCacheConfig {

    /**
     * ehcache 主要的管理器
     *
     * @param ehCacheManagerFactoryBean
     * @return
     */
    @Bean
    public EhCacheCacheManager ehCacheCacheManager(EhCacheManagerFactoryBean ehCacheManagerFactoryBean) {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean.getObject());
    }

    /*
     * 据 shared 与否的设置,
     * Spring 分别通过 CacheManager.create()
     * 或 new CacheManager() 方式来创建一个 ehcache 基地.
     * 也说是说通过这个来设置 cache 的基地是这里的 Spring 独用, 还是跟别的 (如 hibernate 的 Ehcache 共享)
     *
     */
    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache-shiro.xml"));
        cacheManagerFactoryBean.setShared(true);
        return cacheManagerFactoryBean;
    }

}
