package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestBodyReaderAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Log LOG = LogFactory.getLog(RequestBodyReaderAuthenticationFilter.class);

    private static final String ERROR_MESSAGE = "Something went wrong while parsing the custom /login request body";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RequestBodyReaderAuthenticationFilter() {}

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginRequest authRequest = null;
        try {
            final String requestBody = request.getReader().readLine();

            // Handle Spring LoginForm (Optional)
            if (requestBody.contains(getUsernameParameter() + "=") && requestBody.contains(getPasswordParameter() + "=") && requestBody.contains("&")) {
                String username = requestBody.substring(requestBody.indexOf("=") + 1, requestBody.indexOf("&"));
                String password = requestBody.substring(requestBody.lastIndexOf("=") + 1);
                System.out.println("SPRING FORM: " + username);
                System.out.println("SPRING FORM: " + password);
                if (!username.isBlank() && !password.isBlank()) {
                    authRequest = new LoginRequest(username, password);
                }
            } else {
                // Handle Custom POST to /login with a JSON Request Body
                System.out.println("JSON: " + requestBody);
                authRequest = objectMapper.readValue(requestBody, LoginRequest.class);
            }

            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password);

            // Allow subclasses to set the "details" property
            setDetails(request, token);

            Cookie a = new Cookie("Dennis", "ABCDEFGH");
            response.addCookie(a);
            var b = Stream.of(request.getCookies()).map(cookie -> cookie.getName() + " " + cookie.getValue()).collect(Collectors.toList());
            System.out.println("cookies: " + b);


            return this.getAuthenticationManager().authenticate(token);
        } catch(IOException e) {
            LOG.error(ERROR_MESSAGE, e);
            throw new InternalAuthenticationServiceException(ERROR_MESSAGE, e);
        }
    }
}