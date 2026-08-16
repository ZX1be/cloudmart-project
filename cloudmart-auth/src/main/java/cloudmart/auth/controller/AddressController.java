package cloudmart.auth.controller;

import cloudmart.auth.entity.Address;
import cloudmart.auth.entity.User;
import cloudmart.auth.service.AddressService;
import cloudmart.auth.service.UserService;
import common.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/auth/address")
public class AddressController {
    @Autowired
    private AddressService addressService;
    @GetMapping("/info")
    public Result<List<Address>> list(@RequestHeader("X-User-Id") Long userId){
        return Result.success(addressService.listByUserId(userId));
    }

    @PostMapping("/info")
    public Result<Void> save(@RequestHeader("X-User-Id") Long userId,
                             @Valid @RequestBody Address address){
        address.setUserId(userId);
        addressService.saveAddress(address);
        return Result.success();

    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable long id){
        addressService.deleteAddress(id,userId);
        return Result.success();
    }

}
