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

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final BlackListTokenService blackListTokenService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService,
            BlackListTokenService blackListTokenService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.blackListTokenService = blackListTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (blackListTokenService != null && blackListTokenService.isBlackList(token)) {
                    logger.warn("Token is blacklisted/expired");
                    sendUnauthorized(response, "The token has expired. Please log in again.");
                    return;
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userName = jwtTokenProvider.extractUsername(token);

                    if (StringUtils.hasText(userName)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

                        if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            sendUnauthorized(response, "Invalid or expired token.");
                            return;
                        }
                    } else {
                        sendUnauthorized(response, "Invalid token.");
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("JWT authentication failed: " + ex.getMessage(), ex);
            sendUnauthorized(response, "Unauthorized.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String safeMessage = message == null ? "Unauthorized." : message.replace("\"", "\\\"");
        response.getWriter().write("{\"message\":\"" + safeMessage + "\"}");
    }
}
