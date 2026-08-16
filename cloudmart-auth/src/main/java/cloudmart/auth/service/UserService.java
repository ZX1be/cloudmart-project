package cloudmart.auth.service;

import cloudmart.auth.dto.LoginDTO;
import cloudmart.auth.dto.RegisterDTO;
import cloudmart.auth.entity.User;
import cloudmart.auth.vo.LoginVO;

public interface UserService {
    void register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
    User getCurrentUser(Long userId);
    void updateUser(User user);
}