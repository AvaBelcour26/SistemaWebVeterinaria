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

/**
 * Define las reglas de acceso, autenticación y manejo de excepciones de seguridad.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Configura la cadena principal de filtros de seguridad.
     *
     * @param http Configuración de seguridad HTTP
     * @return SecurityFilterChain configurado
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuración de autorizaciones
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // URLs públicas accesibles sin autenticación
                                .requestMatchers(
                                        "/","/about-us","/register","/login",
                                        "/css/**","/js/**","/assets/**",
                                        "/webjars/**", "/error/**","/changeLanguage",
                                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                                // Rutas de administración solo para ADMIN
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                // Rutas comunes para ADMIN y USER
                                .requestMatchers("/mascotas","/citas").hasAnyRole("ADMIN","USER")
                                // Todas las demás rutas requieren autenticación
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
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll()
                )
                // Manejo de excepciones de acceso
                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler(deniedHandler());
                });

        return http.build();
    }

    /**
     * Handler personalizado para redirección después de login exitoso.
     *
     * @return AuthenticationSuccessHandler configurado
     */
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/";

            // Determina la URL de redirección basada en el rol del usuario
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

    /**
     * Handler para manejar accesos denegados.
     *
     * @return AccessDeniedHandler que redirige a página de error 403
     */
    @Bean
    public AccessDeniedHandler deniedHandler() {
        return (request, response, accessDeniedException) -> {
            request.getRequestDispatcher("/error/403").forward(request, response);
        };
    }

    /**
     * Configura el codificador de contraseñas BCrypt.
     *
     * @return PasswordEncoder con algoritmo BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}