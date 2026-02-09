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

    // ✅ Fast “startsWith” checks
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/auth/",
            "/api/public/",
            "/branches/public/",
            "/actuator/",
            "/assets/",
            "/static/",
            "/public/",
            "/webjars/",
            "/css/",
            "/js/",
            "/images/"
    );

    // ✅ Exact public paths
    private static final List<String> PUBLIC_EXACT = List.of(
            "/", "/health", "/index.html", "/favicon.ico", "/error",
            "/account-applications/apply"
    );

    // ✅ Static extensions (covers CDNs / unusual folders)
    private static final List<String> STATIC_EXTENSIONS = List.of(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico",
            ".woff", ".woff2", ".ttf", ".map"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        if (isExactPublicPath(path)) return true;
        if (isPublicPrefix(path)) return true;
        if (isStaticExtension(path)) return true;

        return false;
    }

    private boolean isExactPublicPath(String path) {
        for (String p : PUBLIC_EXACT) {
            if (p.equals(path)) return true;
        }
        return false;
    }

    private boolean isPublicPrefix(String path) {
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    private boolean isStaticExtension(String path) {
        // If it contains a '.', likely a file. Check extensions.
        if (path == null || path.isEmpty() || path.indexOf('.') < 0) return false;
        for (String ext : STATIC_EXTENSIONS) {
            if (path.endsWith(ext)) return true;
        }
        return false;
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
            } catch (Exception ignored) {
                // Invalid/expired token => proceed unauthenticated (no crash)
            }
        }

        chain.doFilter(req, res);
    }
}
