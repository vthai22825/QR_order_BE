package com.example.qr_order.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;


@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest http_servlet_request,
                         HttpServletResponse http_servlet_response,
                         AuthenticationException authentication_exception)
        throws IOException, ServletException{
        http_servlet_response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        http_servlet_response.setContentType("application/json");
        http_servlet_response.setCharacterEncoding("UTF-8");
        http_servlet_response.getWriter().write("{\"message\":\"Unauthorized\"}");
    }

}
