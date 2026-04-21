package es.companeros.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad para la aplicación.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define el bean para el codificador de contraseñas.
     *
     * @return El codificador de contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena de filtros de seguridad.
     *
     * @param http El objeto HttpSecurity para configurar.
     * @return La cadena de filtros de seguridad.
     * @throws Exception Si ocurre un error en la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringAntMatchers("/api/**"))
                .authorizeRequests(authorize -> authorize
                        .antMatchers("/css/**", "/images/**", "/js/**", "/login", "/register").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/join-house", true) // Redirige a join-house, que decidirá si va a /tasks o se queda ahí
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }
}
