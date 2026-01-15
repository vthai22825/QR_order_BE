package com.example.qr_order.security;

import com.example.qr_order.service.BlackListTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt_token_provider;
    private final UserDetailsService user_details_service;
    private final BlackListTokenService black_list_token_service;

    public JwtAuthenticationFilter(JwtTokenProvider jwt_token_provider,
                                   UserDetailsService user_details_service,
                                   BlackListTokenService black_list_token_service) {
        this.jwt_token_provider = jwt_token_provider;
        this.user_details_service = user_details_service;
        this.black_list_token_service = black_list_token_service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filter_chain)
            throws ServletException, IOException {

        try {
            String auth_header = request.getHeader("Authorization");

            // no bearer token -> continue
            if (!StringUtils.hasText(auth_header) || !auth_header.startsWith("Bearer ")) {
                filter_chain.doFilter(request, response);
                return;
            }

            String token = auth_header.substring(7);

            // blacklisted token -> 401
            if (black_list_token_service != null && black_list_token_service.is_black_list(token)) {
                logger.warn("Token is blacklisted/expired");
                send_unauthorized(response, "The token has expired. Please log in again.");
                return;
            }

            // already authenticated -> continue
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filter_chain.doFilter(request, response);
                return;
            }

            String user_name = jwt_token_provider.extractUsername(token);
            if (!StringUtils.hasText(user_name)) {
                send_unauthorized(response, "Invalid token.");
                return;
            }

            UserDetails user_details = user_details_service.loadUserByUsername(user_name);

            if (!jwt_token_provider.isTokenValid(token, user_details)) {
                send_unauthorized(response, "Invalid or expired token.");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user_details,
                            null,
                            user_details.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filter_chain.doFilter(request, response);

        } catch (Exception ex) {
            logger.error("JWT authentication failed: " + ex.getMessage(), ex);
            send_unauthorized(response, "Unauthorized.");
        }
    }

    private void send_unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String safe_message = message == null ? "Unauthorized." : message.replace("\"", "\\\"");
        response.getWriter().write("{\"message\":\"" + safe_message + "\"}");
    }
}
