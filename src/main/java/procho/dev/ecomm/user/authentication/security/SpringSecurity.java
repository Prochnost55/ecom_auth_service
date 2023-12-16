package procho.dev.ecomm.user.authentication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 1. configuration classes will have @Configuration annotation on the top
@Configuration
public class SpringSecurity {

    // 3. Spring security by default restricts access to all the routes.
    // Inorder to keep some routes accessible we have to introduce filters.
    // A filter is also a bean.
    @Bean

    // 4. We can have multiple filtering criteria and @order annotation helps to configure which criteria will be executed first.
    // order will the smallest number will be executed first;
    @Order(1)
    public SecurityFilterChain filteringCriteria(HttpSecurity http) throws Exception {
        http.cors().disable();
        http.csrf().disable();
        // any api with /auth/ will be permitted
        // http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/auth/*").permitAll());
        // any api with /order/ will be validated
        // http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/order/*").authenticated());
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    // 2. Anything which needs to be injected in a spring container it has to initialize with @Bean annotation
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
