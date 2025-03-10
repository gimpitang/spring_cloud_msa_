package com.example.ordersystem.common.config;

import com.example.ordersystem.common.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity // PreAuthorize 사용 시 해당 어노테이션 선언 필요
public class SecurityConfig {
    private final JwtAuthFilter authFilter;

    public SecurityConfig(JwtAuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                //      spring security 에서 cors 정책 지정
                .csrf(AbstractHttpConfigurer::disable) //       csrf 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) //      http basic 보안방식 비뢀성화
                //      세션로그인 방식 사용하지 않는다는 것을 의미
                .sessionManagement(s-> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //      token을 검증하고, token을 통해 Authentication 생성
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                //      .authenticated() : 모든 요청에 대해서 Authentication 객체가 생성되기를 요구.
                .authorizeHttpRequests(a->a.requestMatchers("/member/create","member/doLogin","member/refresh-token", "product/list").permitAll().anyRequest().authenticated())
                .build();
    }


    @Bean
    public PasswordEncoder makePassword() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
