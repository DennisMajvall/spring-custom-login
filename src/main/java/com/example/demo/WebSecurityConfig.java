package com.example.demo;

import com.example.demo.login.RequestBodyReaderAuthenticationFilter;
import com.example.demo.login.UserService;
import com.example.demo.session.CookieUtils;
import com.example.demo.session.SessionAuthenticationFilter;
import com.example.demo.session.SessionAuthenticationProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    public WebSecurityConfig(UserService userService, ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SessionAuthenticationProvider sessionAuthProvider() {
        SessionAuthenticationProvider authProvider = new SessionAuthenticationProvider();
        return authProvider;
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println("configureGlobal sessionAuthenticationProvider");
        auth
                .authenticationProvider(authProvider())
                .authenticationProvider(sessionAuthProvider());
    }

    @Bean
    public SessionAuthenticationFilter sessionAuthenticationFilter() throws Exception {
        SessionAuthenticationFilter authenticationFilter  = new SessionAuthenticationFilter();
        authenticationFilter.setAuthenticationFailureHandler(this::loginFailureHandler);
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }

    @Bean
    public RequestBodyReaderAuthenticationFilter loginAuthenticationFilter() throws Exception {
        RequestBodyReaderAuthenticationFilter authenticationFilter = new RequestBodyReaderAuthenticationFilter();
//        authenticationFilter.setAuthenticationSuccessHandler(this::loginSuccessHandler); // Respond with "the user object"
        authenticationFilter.setAuthenticationFailureHandler(this::loginFailureHandler);
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .authorizeRequests()
                    // TODO: Add more antMatchers here
                    .antMatchers(HttpMethod.GET, "/").permitAll()
                    .antMatchers(HttpMethod.GET, "/super-secret-path").hasRole("ADMIN")
                    .anyRequest().authenticated().and() // Note: This will force authentication on any remaining/non-specified routes

                    // Be careful modifying the 2 addFilterBefore
                    .addFilterBefore(
                            sessionAuthenticationFilter(),
                            BasicAuthenticationFilter.class)
                    .addFilterBefore(
                            loginAuthenticationFilter(),
                            UsernamePasswordAuthenticationFilter.class)
                    .formLogin().and() // Optional
                    // Important (do a GET to the logoutUrl to remove cookies etc)
                    .logout().logoutUrl("/logout").logoutSuccessHandler(this::logoutSuccessHandler);
    }

    private void loginFailureHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException e) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        objectMapper.writeValue(response.getWriter(), e.getMessage());
    }

    private void logoutSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CookieUtils.removeJWTCookie(request, response);

        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getWriter(), "logoutSuccessHandler!");
    }

    // Optional, enable it in loginAuthenticationFilter() above
    private void loginSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        User loggedInUser = userService.findByLogin(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authentication.getName()));

        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getWriter(), loggedInUser);
    }
}