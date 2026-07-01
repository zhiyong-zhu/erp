package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.sales.domain.dto.CustomerAddressRequest;
import com.erp.sales.domain.entity.CustomerAddress;
import com.erp.sales.domain.vo.CustomerAddressVO;
import com.erp.sales.mapper.CustomerAddressMapper;
import com.erp.sales.service.CustomerAddressService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerAddressServiceImpl implements CustomerAddressService {
    private final CustomerAddressMapper customerAddressMapper;

    public CustomerAddressServiceImpl(CustomerAddressMapper customerAddressMapper) {
        this.customerAddressMapper = customerAddressMapper;
    }

    @Override
    public List<CustomerAddressVO> listByCustomer(UUID customerId) {
        return customerAddressMapper.selectList(
                        new LambdaQueryWrapper<CustomerAddress>()
                                .eq(CustomerAddress::getCustomerId, customerId)
                                .orderByDesc(CustomerAddress::getIsDefault)
                                .orderByDesc(CustomerAddress::getCreatedAt))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerAddressVO save(UUID id, CustomerAddressRequest request) {
        CustomerAddress address = id == null ? new CustomerAddress() : customerAddressMapper.selectById(id);
        if (id != null && address == null) {
            throw new BizException(10006, "地址不存在");
        }
        UUID customerId = request.getCustomerId() != null ? request.getCustomerId() : address.getCustomerId();
        if (customerId == null) {
            throw new BizException(10004, "客户不能为空");
        }
        if (address.getId() == null) {
            address.setId(UUID.randomUUID());
            address.setCustomerId(customerId);
            address.setCreatedBy(SecurityUtils.getUserId());
            address.setCreatedAt(OffsetDateTime.now());
        }
        address.setRecipient(request.getRecipient());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        boolean setDefault = Boolean.TRUE.equals(request.getIsDefault());
        address.setIsDefault(setDefault);
        address.setRemark(request.getRemark());
        address.setUpdatedBy(SecurityUtils.getUserId());
        address.setUpdatedAt(OffsetDateTime.now());
        if (id == null) {
            customerAddressMapper.insert(address);
        } else {
            customerAddressMapper.updateById(address);
        }
        // 设为默认时，把该客户其他地址置为非默认
        if (setDefault) {
            customerAddressMapper.update(null,
                    new LambdaUpdateWrapper<CustomerAddress>()
                            .eq(CustomerAddress::getCustomerId, customerId)
                            .ne(CustomerAddress::getId, address.getId())
                            .set(CustomerAddress::getIsDefault, false));
        }
        return toVO(address);
    }

    @Override
    public void delete(UUID id) {
        CustomerAddress address = customerAddressMapper.selectById(id);
        if (address == null) {
            throw new BizException(10006, "地址不存在");
        }
        customerAddressMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(UUID id) {
        CustomerAddress address = customerAddressMapper.selectById(id);
        if (address == null) {
            throw new BizException(10006, "地址不存在");
        }
        // 先把该客户所有地址置非默认，再设当前为默认
        customerAddressMapper.update(null,
                new LambdaUpdateWrapper<CustomerAddress>()
                        .eq(CustomerAddress::getCustomerId, address.getCustomerId())
                        .set(CustomerAddress::getIsDefault, false));
        address.setIsDefault(true);
        address.setUpdatedBy(SecurityUtils.getUserId());
        address.setUpdatedAt(OffsetDateTime.now());
        customerAddressMapper.updateById(address);
    }

    private CustomerAddressVO toVO(CustomerAddress address) {
        CustomerAddressVO vo = new CustomerAddressVO();
        vo.setId(address.getId());
        vo.setCustomerId(address.getCustomerId());
        vo.setRecipient(address.getRecipient());
        vo.setPhone(address.getPhone());
        vo.setAddress(address.getAddress());
        vo.setIsDefault(address.getIsDefault());
        vo.setRemark(address.getRemark());
        vo.setCreatedAt(address.getCreatedAt());
        vo.setUpdatedAt(address.getUpdatedAt());
        return vo;
    }
}
