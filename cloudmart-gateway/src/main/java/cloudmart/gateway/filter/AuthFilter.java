package cloudmart.gateway.filter;

import common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        ServerHttpRequest request=exchange.getRequest();
        String path=request.getURI().getPath();
        if(WHITE_LIST.stream().anyMatch(path::startsWith)){
            return chain.filter(exchange);
        }
        String authHeader=request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader==null ||!authHeader.startsWith("Bearer")){
            return unauthorized(exchange,"未登录");
        }
        String token=authHeader.substring(7);
        if (!JwtUtils.validateToken(token)) {
            return unauthorized(exchange, "Token无效或已过期");
        }
        Claims claims = JwtUtils.parseToken(token);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-Username", claims.get("username", String.class))
                .header("X-Role", claims.get("role", String.class))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());

    }
    @Override
    public int getOrder() {
        return 0;  // 优先级最高
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange,String message){
        ServerHttpResponse response=exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

}
