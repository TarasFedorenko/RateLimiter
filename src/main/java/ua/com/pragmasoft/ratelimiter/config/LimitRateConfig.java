package ua.com.pragmasoft.ratelimiter.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.pragmasoft.ratelimiter.LimitRateFilter;
import ua.com.pragmasoft.ratelimiter.client_key.ClientKeyStrategy;
import ua.com.pragmasoft.ratelimiter.client_key.IPClientKeyStrategy;

/**
 * Configuration class for setting up the rate limiter filter and client key strategy bean.
 */
@Configuration
public class LimitRateConfig {

    /**
     * Registers the {@link LimitRateFilter} with the filter registration bean.
     * Sets up the URL patterns to be filtered and injects the {@link IPClientKeyStrategy}.
     *
     * @param limitRateFilter filter
     * @return the filter registration bean
     */
    @Bean
    public FilterRegistrationBean<LimitRateFilter> rateLimiterFilter(LimitRateFilter limitRateFilter) {
        FilterRegistrationBean<LimitRateFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(limitRateFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    /**
     * Creates and returns an {@link IPClientKeyStrategy} bean.
     *
     * @return the IP client key strategy
     */
    @Bean
    @Qualifier("ipClientKeyStrategy")
    public ClientKeyStrategy ipClientKeyStrategy() {
        return new IPClientKeyStrategy();
    }

    @Bean
    public LimitRateFilter limitRateFilter(@Qualifier("ipClientKeyStrategy") ClientKeyStrategy clientKeyStrategy) {
        return new LimitRateFilter(clientKeyStrategy);
    }
}
