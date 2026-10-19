package com.marketplace;
import java.util.*;import jakarta.servlet.*;import jakarta.servlet.http.*;import java.io.*;
import org.springframework.context.annotation.*;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.web.*;import org.springframework.security.web.csrf.*;import org.springframework.security.crypto.bcrypt.*;import org.springframework.security.crypto.password.*;import org.springframework.web.filter.OncePerRequestFilter;
@Configuration public class Security {
@Bean PasswordEncoder passwords(){return new BCryptPasswordEncoder();}
@Bean SecurityFilterChain chain(HttpSecurity h)throws Exception{
var handler=new CsrfTokenRequestAttributeHandler();
h.authorizeHttpRequests(a->a.anyRequest().permitAll()).csrf(c->c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(handler)).requestCache(c->c.disable()).formLogin(c->c.disable()).httpBasic(c->c.disable()).headers(c->c.contentTypeOptions(o->{}).frameOptions(o->o.deny())).exceptionHandling(c->c.accessDeniedHandler((req,res,e)->{res.setStatus(403);res.setContentType("application/json");res.getWriter().write("{\"code\":\"CSRF_DENIED\",\"message\":\"Refresh the page and try again\"}");}));return h.build();}
@Bean jakarta.servlet.Filter correlation(){return new OncePerRequestFilter(){protected void doFilterInternal(HttpServletRequest q,HttpServletResponse s,FilterChain c)throws ServletException,IOException{s.setHeader("X-Correlation-ID",UUID.randomUUID().toString());c.doFilter(q,s);}};}
}
