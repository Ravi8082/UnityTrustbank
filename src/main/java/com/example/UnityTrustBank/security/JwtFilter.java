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

        // ✅ Public basic endpoints
        if ("/".equals(path) || "/health".equals(path)) return true;
        if ("/index.html".equals(path) || "/favicon.ico".equals(path) || "/error".equals(path)) return true;

        // ✅ Actuator (if you keep it public)
        if (path.startsWith("/actuator/")) return true;

        // ✅ Static resources (most common Spring Boot static mappings)
        if (path.startsWith("/assets/")
                || path.startsWith("/static/")
                || path.startsWith("/public/")
                || path.startsWith("/webjars/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".svg")
                || path.endsWith(".ico")
                || path.endsWith(".woff")
                || path.endsWith(".woff2")
                || path.endsWith(".ttf")
                || path.endsWith(".map")
        ) {
            return true;
        }

        // ✅ Your public APIs
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

        if (header != null
                && header.startsWith("Bearer ")
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
                // ✅ Invalid/expired token => proceed without authentication
                // (no crash / no forced 401 here)
            }
        }

        chain.doFilter(req, res);
    }
}
