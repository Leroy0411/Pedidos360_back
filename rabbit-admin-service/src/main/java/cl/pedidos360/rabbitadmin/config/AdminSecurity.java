package cl.pedidos360.rabbitadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AdminSecurity {
    @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                .httpBasic(Customizer.withDefaults()).build();
    }

    @Bean UserDetailsService users(@Value("${pedidos360.admin.username}") String username,
                                  @Value("${pedidos360.admin.password}") String password) {
        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password))
                .roles("ADMIN").build());
    }
}
