package common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class JwtUtils {
    private static final String SECRET="cloudmart-jwt-secret-key-2026-very-long!!";
    private static final long EXPIRE=7*24*60*60*1000L; //7天
    //生成HMAC-SHA密钥
    private static SecretKey getKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    //生成Token
    public static String generateToken(Long userId, String name, Map<String, Object> claims){
        return Jwts.builder().
                claims(claims).
                subject(String.valueOf(userId)).
                issuedAt(new Date()).
                expiration(new Date(System.currentTimeMillis()+EXPIRE)).
                signWith(getKey()).
                compact();

    }
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, Map.of("username", username));
    }
    //解析Token
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    // 从Token获取userId
    public static Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }
    //校验Token是否有效
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
