package cloudmart.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String avatar;
    private String role;
}
