package com.example.demo;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CustomFilter extends OncePerRequestFilter {
 
    @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        Optional<Cookie> dennisCookie = Stream.of(request.getCookies()).filter(cookie -> cookie.getName().equals("Dennis")).findFirst();
        if (dennisCookie.isPresent()){
            System.out.println("Cookie found: " + dennisCookie.get().getValue());

            Authentication authentication
                    = new UsernamePasswordAuthenticationToken(
                            "username", "password",
                    List.of(AuthorityUtils.createAuthorityList("USER")).get(0));
            authentication.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            System.out.println("Cookie not found");
        }
        chain.doFilter(request, response);
    }
}