package ua.com.pragmasoft.ratelimiter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up the rate limiter filter and client key strategy bean.
 */
@Configuration
public class LimitRateConfig {

    /**
     * Registers the {@link LimitRateFilter} with the filter registration bean.
     * Sets up the URL patterns to be filtered and injects the {@link IPClientKeyStrategy}.
     *
     * @param clientKeyStrategy the strategy for extracting client keys
     * @return the filter registration bean
     */
    @Bean
    public FilterRegistrationBean<LimitRateFilter> rateLimiterFilter(@Qualifier("ipClientKeyStrategy") ClientKeyStrategy clientKeyStrategy) {
        FilterRegistrationBean<LimitRateFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LimitRateFilter(clientKeyStrategy));
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
}
