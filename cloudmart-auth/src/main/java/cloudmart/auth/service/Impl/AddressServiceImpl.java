package cloudmart.auth.service.Impl;

import cloudmart.auth.entity.Address;
import cloudmart.auth.mapper.AddressMapper;
import cloudmart.auth.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressMapper addressMapper;

    private static final int MAX_ADDRESS=20;
    @Override
    public List<Address> listByUserId(Long UserId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId,UserId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt);
        return addressMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void saveAddress(Address address) {
        // 限制最多 20 个地址
        long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, address.getUserId()));

        if (count>MAX_ADDRESS){
            throw new BizException("收货地址最多20个");
        }
        if(address.getIsDefault()==1){
            cancelDefault(address.getUserId());
        }
        addressMapper.insert(address);
    }

    @Override
    @Transactional
    public void updateAddress(Address address) {
        Address exist=addressMapper.selectById(address.getId());
        if(exist==null || !exist.getUserId().equals(address.getUserId())){
            throw new BizException("地址不存在");
        }
        if(address.getIsDefault()==1){
            cancelDefault(address.getUserId());
        }
        addressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Long id, Long userId) {
        Address exist=addressMapper.selectById(id);
        if(exist==null || !exist.getUserId().equals(userId)){
            throw new BizException("地址不存在");
        }
        addressMapper.deleteById(id);
    }

    private void cancelDefault(Long userId){
        Address update = new Address();
        update.setIsDefault(0);
        LambdaQueryWrapper<Address> wrapper= new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId,userId)
                .eq(Address::getIsDefault,1);
        addressMapper.update(update,wrapper);
    }

}
