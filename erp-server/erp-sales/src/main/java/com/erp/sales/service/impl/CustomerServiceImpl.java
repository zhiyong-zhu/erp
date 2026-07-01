package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.sales.domain.dto.CustomerRequest;
import com.erp.sales.domain.entity.Customer;
import com.erp.sales.domain.vo.CustomerVO;
import com.erp.sales.mapper.CustomerMapper;
import com.erp.sales.service.CustomerService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    public PageVO<CustomerVO> list(long pageNum, long pageSize, String name) {
        Page<Customer> page = customerMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Customer>()
                        .like(name != null && !name.isBlank(), Customer::getName, name)
                        .orderByDesc(Customer::getCreatedAt));
        List<CustomerVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public CustomerVO save(UUID id, CustomerRequest request) {
        Customer customer = id == null ? new Customer() : customerMapper.selectById(id);
        if (id != null && customer == null) {
            throw new BizException(10006, "客户不存在");
        }
        if (customer.getId() == null) {
            customer.setId(UUID.randomUUID());
            customer.setCreatedBy(SecurityUtils.getUserId());
            customer.setCreatedAt(OffsetDateTime.now());
        }
        // 编码留空时自动生成（C + 4 位序号，按已有编码取最大序号 +1）
        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = generateNextCode();
        }
        customer.setCode(code);
        customer.setName(request.getName());
        customer.setShortName(request.getShortName());
        customer.setCustomerType(request.getCustomerType());
        customer.setContactPerson(request.getContactPerson());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setGrade(request.getGrade());
        customer.setPaymentTerms(request.getPaymentTerms());
        customer.setSalesRepId(request.getSalesRepId());
        customer.setTaxNumber(request.getTaxNumber());
        customer.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        customer.setRemark(request.getRemark());
        customer.setUpdatedBy(SecurityUtils.getUserId());
        customer.setUpdatedAt(OffsetDateTime.now());
        if (id == null) {
            customerMapper.insert(customer);
        } else {
            customerMapper.updateById(customer);
        }
        return toVO(customer);
    }

    private CustomerVO toVO(Customer customer) {
        CustomerVO vo = new CustomerVO();
        vo.setId(customer.getId());
        vo.setCode(customer.getCode());
        vo.setName(customer.getName());
        vo.setShortName(customer.getShortName());
        vo.setCustomerType(customer.getCustomerType());
        vo.setContactPerson(customer.getContactPerson());
        vo.setPhone(customer.getPhone());
        vo.setEmail(customer.getEmail());
        vo.setAddress(customer.getAddress());
        vo.setGrade(customer.getGrade());
        vo.setPaymentTerms(customer.getPaymentTerms());
        vo.setSalesRepId(customer.getSalesRepId());
        vo.setTaxNumber(customer.getTaxNumber());
        vo.setStatus(customer.getStatus());
        vo.setRemark(customer.getRemark());
        vo.setCreatedAt(customer.getCreatedAt());
        vo.setUpdatedAt(customer.getUpdatedAt());
        return vo;
    }

    /**
     * 生成下一个客户编码：C + 4 位序号（如 C0001）。
     * 取所有以 "C" 开头且后接数字的编码中序号最大值 +1，不存在则从 1 开始。
     */
    private String generateNextCode() {
        Customer latest = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>()
                        .likeRight(Customer::getCode, "C")
                        .orderByDesc(Customer::getCode)
                        .last("LIMIT 1"));
        int next = 1;
        if (latest != null && latest.getCode() != null) {
            String tail = latest.getCode().substring(1);
            try {
                next = Integer.parseInt(tail) + 1;
            } catch (NumberFormatException ignored) {
                // 编码非纯数字后缀时从 1 重新开始，后续仍有唯一约束兜底
            }
        }
        return String.format("C%04d", next);
    }
}
