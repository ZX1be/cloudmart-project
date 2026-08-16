package cloudmart.auth.controller;

import cloudmart.auth.entity.User;
import cloudmart.auth.service.UserService;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("X-User-Id") Long userId){
        return  Result.success(userService.getCurrentUser(userId));
    }
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestHeader("X-User-Id") Long userId,
                                       @RequestBody User user){
        user.setId(userId);
        user.setPassword(null);
        user.setRole(null);
        userService.updateUser(user);
        return Result.success();

    }
}
