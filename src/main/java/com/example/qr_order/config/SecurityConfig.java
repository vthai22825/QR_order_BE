package com.example.qr_order.config;


import com.example.qr_order.security.JwtAuthenticationEntryPoint;
import com.example.qr_order.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.status.StatusLogger;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   private final JwtAuthenticationFilter jwt_authentication_filter;
   private final JwtAuthenticationEntryPoint jwt_authentication_entry_point;

   public SecurityConfig(JwtAuthenticationEntryPoint jwt_authentication_entry_point,
                         JwtAuthenticationFilter jwt_authentication_filter){
       this.jwt_authentication_filter = jwt_authentication_filter;
       this.jwt_authentication_entry_point = jwt_authentication_entry_point;
   }

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http_security) throws Exception{
       http_security
               .cors(Customizer.withDefaults())
               .csrf(csrf -> csrf.disable())
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .formLogin(form -> form.disable())
               .httpBasic(basic -> basic.disable())

               .exceptionHandling(ex -> ex
                       .authenticationEntryPoint(jwt_authentication_entry_point)
                       .accessDeniedHandler((request, response, accessDeniedException) -> {
                           response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                           response.setContentType("application/json");
                           response.setCharacterEncoding("UTF-8");
                           response.getWriter().write("{\"message\":\"Forbidden\"}");
                       })
               )
               .authorizeHttpRequests(auth -> auth
                       .requestMatchers("/api/auth/**").permitAll()
                       .requestMatchers("/api/public/**").permitAll()

                       .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                       .requestMatchers("/api/admin/**").hasRole("OWNER")
                       .requestMatchers("/api/cashier/**").hasAnyRole("OWNER","CASHIER")
                       .requestMatchers("/api/server/**").hasAnyRole("OWNER","CASHIER","SERVER")

                       .anyRequest().authenticated()
               )
               .addFilterBefore(jwt_authentication_filter, UsernamePasswordAuthenticationFilter.class);
       return http_security.build();
   }

   @Bean
    public PasswordEncoder passwordEncoder(){
       return new BCryptPasswordEncoder();
   }

   @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authentication_configuration)
       throws Exception{
       return authentication_configuration.getAuthenticationManager();
   }

   @Bean
    public CorsConfigurationSource cors_configuration_source(){
       CorsConfiguration cors_config = new CorsConfiguration();
       cors_config.setAllowedOrigins(List.of("http://localhost:4200"));
       cors_config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
       cors_config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
       cors_config.setAllowCredentials(true);

       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", cors_config);

       return source;
   }
}
