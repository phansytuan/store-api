package com.interview.demo.config;

import com.interview.demo.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration
 *
 * IoC demo: Spring inject tất cả Bean qua constructor.
 * Bean lifecycle: SecurityFilterChain là Singleton.
 */
@Configuration            // Đây là class cấu hình Spring (chứa @Bean)
@EnableWebSecurity        // Bật Spring Security cho ứng dụng web
@EnableMethodSecurity     // Cho phép dùng @PreAuthorize ở Controller/Service
@RequiredArgsConstructor  // Lombok: tạo constructor inject 2 field final bên dưới
public class SecurityConfig {

    private final JwtAuthFilter      jwtAuthFilter;       // Filter tự viết: xác thực JWT
    private final UserDetailsService userDetailsService;  // Load user từ DB

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http    
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll() // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()

                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // Protected
                .anyRequest().authenticated()
            )
            .headers(h -> h
                .frameOptions(fo -> fo
                    .disable())) // H2 console
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

/** 🔄 Luồng hoạt động tổng thể

    HTTP Request
        ↓
    JwtAuthFilter           ← Đọc token, validate, set SecurityContext
        ↓
    SecurityFilterChain     ← Kiểm tra URL có được phép không
        ↓
    Controller              ← @PreAuthorize kiểm tra thêm nếu cần
        ↓
    AuthenticationManager   ← Dùng khi gọi /auth/login
        ↓
    DaoAuthenticationProvider → UserDetailsService → DB
*/
