package ai.momoyeyu.figspace.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Junjie Su
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT令牌
     * @param userAccount 用户账号；需要保证唯一，否则令牌会出错
     * @return JWT令牌
     */
    public String generateToken(String userAccount) {
        Map<String, Object> claims = new HashMap<>();
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userAccount)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证令牌是否合法，包括检测用户是否对应，令牌是否过期
     * @param token JWT令牌
     * @param userAccount 用户账号
     * @return boolean 是否合法
     */
    public boolean validateToken(String token, String userAccount) {
        final String tokenuserAccount = getUserAccountFromToken(token);
        return (tokenuserAccount.equals(userAccount) && !isTokenExpired(token));
    }

    /**
     * 通过令牌获取用户名
     * @param token JWT令牌
     * @return userAccount 用户账号
     */
    public String getUserAccountFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getSubject(); // 创建令牌时存储的是 userAccount
    }

    /**
     * 通过令牌获取Claims，供进一步获取其他数据
     * @param token JWT令牌
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 内部方法，检查令牌是否过期；过期则返回 true，否则返回 false
     * @param token JWT令牌
     * @return boolean 是否过期
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date()); // 过期则返回 true
    }

}
