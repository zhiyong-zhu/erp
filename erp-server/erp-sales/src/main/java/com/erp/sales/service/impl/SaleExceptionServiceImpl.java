package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.sales.domain.dto.SaleExceptionHandleRequest;
import com.erp.sales.domain.entity.SaleException;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.entity.SaleReturnItem;
import com.erp.sales.domain.vo.SaleExceptionVO;
import com.erp.sales.mapper.SaleExceptionMapper;
import com.erp.sales.service.SaleExceptionService;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleExceptionServiceImpl implements SaleExceptionService {
    private final SaleExceptionMapper saleExceptionMapper;

    public SaleExceptionServiceImpl(SaleExceptionMapper saleExceptionMapper) {
        this.saleExceptionMapper = saleExceptionMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOrderException(SaleOrder order, SaleOrderItem item, String exceptionType, String description) {
        SaleException exception = baseException(exceptionType, description);
        exception.setSaleOrderId(order.getId());
        exception.setCustomerId(order.getCustomerId());
        exception.setCustomerName(order.getCustomerName());
        if (item != null) {
            exception.setSaleOrderItemId(item.getId());
            exception.setSkuId(item.getSkuId());
            exception.setSkuCode(item.getSkuCode());
            exception.setProductName(item.getProductName());
        }
        saleExceptionMapper.insert(exception);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createReturnException(SaleReturn saleReturn, SaleReturnItem item, String exceptionType, String description) {
        SaleException exception = baseException(exceptionType, description);
        exception.setSaleOrderId(saleReturn.getSaleOrderId());
        exception.setSaleReturnId(saleReturn.getId());
        exception.setCustomerId(saleReturn.getCustomerId());
        exception.setCustomerName(saleReturn.getCustomerName());
        if (item != null) {
            exception.setSaleReturnItemId(item.getId());
            exception.setSaleOrderItemId(item.getSaleOrderItemId());
            exception.setSkuId(item.getSkuId());
            exception.setSkuCode(item.getSkuCode());
            exception.setProductName(item.getProductName());
        }
        saleExceptionMapper.insert(exception);
    }

    @Override
    public PageVO<SaleExceptionVO> list(long pageNum, long pageSize) {
        Page<SaleException> page = saleExceptionMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SaleException>().orderByDesc(SaleException::getCreatedAt)
        );
        List<SaleExceptionVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SaleExceptionVO handle(UUID id, SaleExceptionHandleRequest request) {
        SaleException exception = saleExceptionMapper.selectById(id);
        if (exception == null) {
            throw new BizException(10006, "销售异常不存在");
        }
        switch (request.getAction()) {
            case "resolve" -> exception.setStatus("RESOLVED");
            case "close" -> exception.setStatus("CLOSED");
            default -> throw new BizException(10004, "不支持的异常处理动作");
        }
        exception.setResolution(request.getResolution());
        exception.setHandledBy(SecurityUtils.getUserId());
        exception.setHandledAt(OffsetDateTime.now());
        saleExceptionMapper.updateById(exception);
        return toVO(exception);
    }

    private SaleException baseException(String exceptionType, String description) {
        SaleException exception = new SaleException();
        exception.setId(UUID.randomUUID());
        exception.setExceptionNo("SEX-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + UUID.randomUUID().toString().substring(0, 8));
        exception.setExceptionType(exceptionType);
        exception.setStatus("OPEN");
        exception.setDescription(description);
        exception.setCreatedBy(SecurityUtils.getUserId());
        exception.setCreatedAt(OffsetDateTime.now());
        return exception;
    }

    private SaleExceptionVO toVO(SaleException exception) {
        SaleExceptionVO vo = new SaleExceptionVO();
        vo.setId(exception.getId());
        vo.setExceptionNo(exception.getExceptionNo());
        vo.setSaleOrderId(exception.getSaleOrderId());
        vo.setSaleOrderItemId(exception.getSaleOrderItemId());
        vo.setSaleReturnId(exception.getSaleReturnId());
        vo.setSaleReturnItemId(exception.getSaleReturnItemId());
        vo.setCustomerName(exception.getCustomerName());
        vo.setSkuCode(exception.getSkuCode());
        vo.setProductName(exception.getProductName());
        vo.setExceptionType(exception.getExceptionType());
        vo.setStatus(exception.getStatus());
        vo.setDescription(exception.getDescription());
        vo.setResolution(exception.getResolution());
        vo.setCreatedAt(exception.getCreatedAt());
        vo.setHandledAt(exception.getHandledAt());
        return vo;
    }
}
