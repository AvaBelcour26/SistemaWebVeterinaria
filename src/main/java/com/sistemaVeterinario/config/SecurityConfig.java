package com.sistemaVeterinario.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/","/about-us","/register","/login","/css/**","/js/**"
                                        ,"/assets/**", "/webjars/**", "/error/**","/changeLanguage").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/mascotas","/citas").hasAnyRole("ADMIN","USER")
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                                .successHandler(successHandler())
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/") //URL de redirección después de cerrar sesión(Contenido publico).
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID") //Invalidar sesión después de cerrar sesión.
                                .permitAll()
                )
                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler(deniedHandler());
                });

        return http.build();
    }
    /*Handlers*/
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/";

            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    redirectUrl = "/admin/usuarios";
                    break;
                } else if (authority.getAuthority().equals("ROLE_USER")) {
                    redirectUrl = "/mascotas";
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }
    @Bean
    public AccessDeniedHandler deniedHandler() {
        return (request, response, accessDeniedException) -> {
            request.getRequestDispatcher("/error/403").forward(request, response);
        };
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}