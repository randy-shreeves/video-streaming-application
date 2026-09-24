package com.randyshreeves.videostreaming.config;

import com.randyshreeves.videostreaming.auth.JwtAuthenticationFilter;
import com.randyshreeves.videostreaming.auth.StreamTokenAuthenticationFilter;
import com.randyshreeves.videostreaming.user.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final StreamTokenAuthenticationFilter streamTokenAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            StreamTokenAuthenticationFilter streamTokenAuthenticationFilter
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.streamTokenAuthenticationFilter = streamTokenAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(streamTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/movies").authenticated()
                    .requestMatchers(HttpMethod.GET, "/movies/*/details").authenticated()
                    .requestMatchers(HttpMethod.GET, "/movies/*/poster").authenticated()
                    .requestMatchers(HttpMethod.GET, "/movies/*/stream").authenticated()
                    .requestMatchers(HttpMethod.GET, "/movies/*/stream-token").authenticated()
                    .requestMatchers(HttpMethod.GET, "/watchlist").authenticated()
                    .requestMatchers(HttpMethod.POST, "/watchlist/*").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/watchlist/*").authenticated()
                    .requestMatchers(HttpMethod.POST, "/movies/*/poster").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/movies/*/video").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/movies").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/movies/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/movies/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/movies/admin").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/movies/admin/*/details").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/movies/admin/*/poster").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(
                            (request, response, authException) -> {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    })
                    .accessDeniedHandler(
                            (request, response, accessDeniedException) -> {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    })
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
