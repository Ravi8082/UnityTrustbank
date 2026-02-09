// JwtFilter.java
package com.example.UnityTrustBank.security;

import java.io.IOException;
import java.util.List;

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

    private static final List<String> PUBLIC_EXACT = List.of(
            "/", "/health", "/index.html", "/favicon.ico", "/error"
    );

    private static final List<String> PUBLIC_PREFIXES = List.of(
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

    private static final List<String> STATIC_EXTENSIONS = List.of(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico",
            ".woff", ".woff2", ".ttf", ".map"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null) return false;

        // ✅ exact allow
        for (String p : PUBLIC_EXACT) {
            if (p.equals(path)) return true;
        }

        // ✅ prefix allow
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }

        // ✅ extension allow
        if (path.indexOf('.') >= 0) {
            for (String ext : STATIC_EXTENSIONS) {
                if (path.endsWith(ext)) return true;
            }
        }

        // ✅ allow this public endpoint too
        if ("/account-applications/apply".equals(path)) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        // No bearer token => continue (Security will enforce auth where needed)
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
            // Invalid/expired token => proceed unauthenticated (no crash)
        }

        chain.doFilter(req, res);
    }
}
