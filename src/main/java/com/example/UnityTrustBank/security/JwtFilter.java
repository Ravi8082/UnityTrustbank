package com.example.UnityTrustBank.security;

import java.io.IOException;
import java.util.Set;

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

    private static final Set<String> PUBLIC_EXACT = Set.of(
            "/", "/health", "/index.html", "/favicon.ico", "/error",
            "/account-applications/apply"
    );

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/actuator/",
            "/auth/",
            "/api/public/",
            "/branches/public/",
            "/assets/",
            "/static/",
            "/public/",
            "/webjars/",
            "/css/",
            "/js/",
            "/images/"
    );

    private static final Set<String> STATIC_EXTENSIONS = Set.of(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico",
            ".woff", ".woff2", ".ttf", ".map"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null) return false;

        if (PUBLIC_EXACT.contains(path)) return true;

        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }

        int dot = path.lastIndexOf('.');
        if (dot >= 0) {
            String ext = path.substring(dot);
            if (STATIC_EXTENSIONS.contains(ext)) return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

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
        } catch (Exception ignored) {
            // invalid/expired token => continue unauthenticated
        }

        chain.doFilter(req, res);
    }
}
