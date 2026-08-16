package cloudmart.auth.controller;

import cloudmart.auth.dto.LoginDTO;
import cloudmart.auth.dto.RegisterDTO;
import cloudmart.auth.service.UserService;
import cloudmart.auth.vo.LoginVO;
import common.exception.BizException;
import common.result.Result;
import common.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto){
            userService.register(dto);
            return Result.success("注册成功",null);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto){
        LoginVO vo=userService.login(dto);
        return Result.success(vo);
    }

    @PostMapping("/refresh")
    public Result<String> refresh(@RequestHeader("Authorization") String token){
        token=token.replace("Bearer ","");
        if(!JwtUtils.validateToken(token)){
            throw  new BizException(401,"Token无效或已过期");
        }
        Long userId=JwtUtils.getUserId(token);
        String username=userService.getCurrentUser(userId).getUsername();
        String newToken = JwtUtils.generateToken(userId, username);
        return Result.success(newToken);
    }

}
