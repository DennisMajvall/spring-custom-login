package com.example.demo.login;

import com.example.demo.session.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestBodyReaderAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

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
//                System.out.println("Spring form username: " + username + ", password: " + password);
                if (!username.isBlank() && !password.isBlank()) {
                    authRequest = new LoginRequest(username, password);
                }
            } else {
                // Handle Custom POST to /login with a JSON Request Body
//                System.out.println("JSON: " + requestBody);
                authRequest = objectMapper.readValue(requestBody, LoginRequest.class);
            }

            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password);

            // Allow subclasses to set the "details" property
            setDetails(request, token);
            CookieUtils.addJWTCookie(response, authRequest.username);

            return this.getAuthenticationManager().authenticate(token);
        } catch(IOException e) {
            System.out.println(ERROR_MESSAGE + " - " + e.getMessage());
            throw new InternalAuthenticationServiceException(ERROR_MESSAGE, e);
        }
    }
}