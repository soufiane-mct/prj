package com.aliboo.book.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.expiration}") //hna hit kola mera aytbdl token dkshi elsh drnah f application.yml
    private long jwtExpiration;

    @Value("${application.security.jwt.secret-key}") //hdi drna secretkey f application-dev.yml (ra ba9i endk err)
    private String secretKey;


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // hit drna f jwts setSubject(userDetails.getUsername()) y3ni Subject yjib lina user mn userdetails
    }

    public <T> T extractClaim(String token, Function<Claims , T> claimResolver) { // T a type dyalha w hdi sminaha claimResolver
        // Claims type dyalha
        final Claims claims = extractAllClaims(token);//andiro method kat jib lina gae claims mn token o dirha f var claims
        return claimResolver.apply(claims);

    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) //hda li khdmna bih bsh drna sign l jwtoken dylna ltht gae
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String,Object> claims, UserDetails userDetails) { //hna ankhdmo b user details bsh nakhdo information li kat3tina spring o derna HashMap bsh nakhdo ta extra information f jwt dyalna

        return buildTocken(claims, userDetails, jwtExpiration);//hna buildtocken fih extra claims li drna o ykhd userdetails d spring o ykhd ta jwtExpiration li drna bch lbghina n jeneriw wl nbdlo jwttokon f cases akherin
    }

    private String buildTocken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long jwtExpiration)
    {//hna andiro athority l token dyalna
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();//hna drna list d authority
        return //hna andiro build l jwtToken dyalna
                Jwts
                        .builder()
                        .setClaims(extraClaims) //hna jib lina extraCases li derna dyalna
                        .setSubject(userDetails.getUsername()) //hna jib lina l id dyl token d user dylna 3an tari9 userdetails
                        .setIssuedAt(new Date(System.currentTimeMillis()))//w9ita li t creea token
                        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))//shal ayb9a token 9bl may t expira
                        .claim("authorities" , authorities)//hna kdir extra claims authoritis lihia authorities li drna fo9f var
                        .signWith(getSignInKey())//hna mohima kndiro sign l token dyalna 3an tari9 key li drnah 3an tari9 method getSignInKey li derna
                        .compact()
                ;
    }

    public boolean isTokenValid(String token , UserDetails userDetails){
        final String username = extractUsername(token); //hna jib lia username wl mail mn token lkn token valid 3an tari9 extractUsername lidrna fo9
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) ;//hna kat checki wsh user howa nit li weslna o wsh t expira 3an tari9 function li derna isTokenExpired
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);//jib lina date expiration mn clams li drna f token jwts
    }


    private Key getSignInKey() { //hd method khas trj3 lina key li andiroha f jwt
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey); //hna decodi lina secretkey  dylna li derna lihowa token dyalna
        return Keys.hmacShaKeyFor(keyBytes); //hna l incoding l keyBytes li hia li drna fo9
        //y3ni hna drna signkey dyalna o ankhedmo bih bsh ndiro sign l token dyalna (khdmna bih f Jwts.signWith(getSignInKey()) ra fo9)
    }
}
