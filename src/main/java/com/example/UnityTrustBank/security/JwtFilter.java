package com.example.UnityTrustBank.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // ✅ allow root + health + actuator
        if ("/".equals(path) || "/health".equals(path)) return true;
        if ("/index.html".equals(path) || "/favicon.ico".equals(path) || "/error".equals(path)) return true;
        if (path.startsWith("/actuator/")) return true;

        // ✅ allow common static paths (Spring Boot serves these)
        if (path.startsWith("/assets/") || path.startsWith("/static/")
                || path.startsWith("/public/") || path.startsWith("/webjars/")) return true;

        // ✅ allow your public APIs
        return path.startsWith("/auth/")
            || path.equals("/account-applications/apply")
            || path.startsWith("/api/public/")
            || path.startsWith("/branches/public/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(7);

            try {
                if (jwtUtil.validate(token)) {
                    String email = jwtUtil.extractEmail(token);
                    var userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ex) {
                // ✅ if token invalid/expired, just continue without auth (no crash)
                // optionally: res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return;
            }
        }

        chain.doFilter(req, res);
    }
}
