package edu.neu.coe.info7255bda.filter;

import edu.neu.coe.info7255bda.utils.jwt.JWTUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    @Value("${excl-url}")
    private String excludeUrl;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain){
        String path = request.getServletPath();
        if (excludeUrl.contains(path)){
            filterChain.doFilter(request, response);
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String auth = request.getHeader("Authorization");
        if ((auth != null) && (auth.length() > 7)){
            String HeadStr = auth.substring(0, 6).toLowerCase();
            if (HeadStr.compareTo("bearer") == 0) {
                String jwt = auth.substring(7);
                if (jwtUtil.validateToken(path, jwt)){
                    log.info("Authorization success!!!");
                    response.setStatus(HttpServletResponse.SC_OK);
                    if (!checkIfMatch(request)){
                        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    }
                    filterChain.doFilter(request, response);
                }
            }
        }
    }

    private boolean checkIfMatch(HttpServletRequest request){
        String method = request.getMethod().toUpperCase();
        if (method.equals("PUT") || method.equals("PATCH")){
            return request.getHeader("If-Match") != null;
        }
        return true;
    }
}
