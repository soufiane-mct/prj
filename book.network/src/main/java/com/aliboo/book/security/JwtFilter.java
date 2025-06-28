package com.aliboo.book.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor

//hna hd classaredoha filter
public class JwtFilter extends OncePerRequestFilter {

    //blast mdirconstracture
    private final  JwtService jwtService;
 //   @Autowired //blast mdirconstracture
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)  //hna filter dyalna fih 3 parametre requist response filter o ankhdmo bihom ltht
            throws ServletException, IOException {//hna ankhdmo b hd parametre li drna fo9
        if(request.getServletPath().contains("/api/v1/auth")){
            filterChain.doFilter(request, response);//hna ktgolih la dkhl user l dak path khedm lia filter (o dir request o response)
            return;
        }
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwt;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { //lkn l authHeader mkynsh wl la mknsh kibda b Bearer
            filterChain.doFilter(request, response); //dir lih filter ewtani
            return;
        }
        jwt = authHeader.substring(7);//hna bsh ybda jwt hesab mn harf sab3 7 (hit drna Bearer )
        userEmail = jwtService.extractUsername(jwt);//hna ayjib lina user name mn jwt 3an tari9 l method li drna o sminaha extractUsername mn class JwtService liderna
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){//la mknsh null o mykonsh authenticated
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);//hna an fetshiw user mn database 3n tari9 useremail
            if (jwtService.isTokenValid(jwt,userDetails)){ //lkn l user valid o jwt tahowa
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( //hd obj ankhdmo bih f login
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken); //hna adir auth l user
            }
        }
        filterChain.doFilter(request, response); //hna n3iyto l shi lakher dl filterchain

    }
}
