package br.com.fintrack.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.expiration-ms}") private long expiration;
    private SecretKey key(){return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));}
    public String generateToken(UserDetails user){
        Date now=new Date();
        return Jwts.builder().subject(user.getUsername()).issuedAt(now)
            .expiration(new Date(now.getTime()+expiration)).signWith(key()).compact();
    }
    public String extractUsername(String token){
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
    }
    public boolean isValid(String token, UserDetails user){
        try { var claims=Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
            return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().after(new Date());
        } catch(Exception e){return false;}
    }
}
