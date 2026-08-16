package cloudmart.auth.service;

import cloudmart.auth.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> listByUserId(Long UserId);
    void saveAddress(Address address);
    void updateAddress(Address address);
    void deleteAddress(Long id, Long userId);
}
