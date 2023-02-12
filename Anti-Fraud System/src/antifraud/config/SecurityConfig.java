package antifraud.config;


import antifraud.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
@Configuration
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final PasswordEncoderProvider passwordEncoderProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService) // user store
                .passwordEncoder(passwordEncoderProvider.getEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests() // manage access
                .antMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .antMatchers("/actuator/shutdown").permitAll() // needs to run test
                // other matchers
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }
}