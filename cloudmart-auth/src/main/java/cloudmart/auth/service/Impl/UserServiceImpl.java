package cloudmart.auth.service.Impl;

import cloudmart.auth.dto.LoginDTO;
import cloudmart.auth.dto.RegisterDTO;
import cloudmart.auth.entity.User;
import cloudmart.auth.mapper.UserMapper;
import cloudmart.auth.service.UserService;
import cloudmart.auth.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.exception.BizException;
import common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //注册
    @Override
    public void register(RegisterDTO dto){
        LambdaQueryWrapper<User> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername());
        if (userMapper.selectCount(wrapper)>0){
            throw new BizException("用户名已存在");
        }
        if(dto.getPhone()!=null){
            wrapper.clear();
            wrapper.eq(User::getPhone,dto.getPhone());
            if(userMapper.selectCount(wrapper)>0){
                throw new BizException("手机号已注册");
            }
        }
        User user=new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // BCrypt 加密
        user.setPhone(dto.getPhone());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
    }

    //登录
    @Override
    public LoginVO login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BizException("账号已被禁用");
        }
        // 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        // 生成 Token
        String token = JwtUtils.generateToken(
                user.getId(),
                user.getUsername(),
                Map.of("role", user.getRole())
        );
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }
    //获取当前用户信息
    @Override
    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(null);  // 防止密码泄露
        return user;
    }


    @Override
    public void updateUser(User user) {
        userMapper.updateById(user);
    }


}
