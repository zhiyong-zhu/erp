package com.erp.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.purchase.domain.dto.PurchaseExceptionHandleRequest;
import com.erp.purchase.domain.entity.PurchaseException;
import com.erp.purchase.domain.entity.PurchaseOrder;
import com.erp.purchase.domain.entity.PurchaseOrderItem;
import com.erp.purchase.domain.vo.PurchaseExceptionVO;
import com.erp.purchase.mapper.PurchaseExceptionMapper;
import com.erp.purchase.service.PurchaseExceptionService;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PurchaseExceptionServiceImpl implements PurchaseExceptionService {
    private final PurchaseExceptionMapper purchaseExceptionMapper;

    public PurchaseExceptionServiceImpl(PurchaseExceptionMapper purchaseExceptionMapper) {
        this.purchaseExceptionMapper = purchaseExceptionMapper;
    }

    @Override
    public void createInspectionException(PurchaseOrder order, PurchaseOrderItem item, String description) {
        PurchaseException exception = new PurchaseException();
        exception.setId(UUID.randomUUID());
        exception.setExceptionNo("PEX-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        exception.setPurchaseOrderId(order.getId());
        exception.setPurchaseOrderItemId(item.getId());
        exception.setSupplierId(order.getSupplierId());
        exception.setSupplierName(order.getSupplierName());
        exception.setMaterialId(item.getMaterialId());
        exception.setMaterialCode(item.getMaterialCode());
        exception.setMaterialName(item.getMaterialName());
        exception.setExceptionType("INSPECTION");
        exception.setStatus("OPEN");
        exception.setDescription(description);
        exception.setCreatedBy(SecurityUtils.getUserId());
        exception.setCreatedAt(OffsetDateTime.now());
        purchaseExceptionMapper.insert(exception);
    }

    @Override
    public PageVO<PurchaseExceptionVO> list(long pageNum, long pageSize) {
        Page<PurchaseException> page = purchaseExceptionMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<PurchaseException>().orderByDesc(PurchaseException::getCreatedAt)
        );
        List<PurchaseExceptionVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PurchaseExceptionVO handle(UUID id, PurchaseExceptionHandleRequest request) {
        PurchaseException exception = purchaseExceptionMapper.selectById(id);
        if (exception == null) {
            throw new BizException(10006, "采购异常不存在");
        }
        switch (request.getAction()) {
            case "resolve" -> exception.setStatus("RESOLVED");
            case "close" -> exception.setStatus("CLOSED");
            default -> throw new BizException(10004, "不支持的异常处理动作");
        }
        exception.setResolution(request.getResolution());
        exception.setHandledBy(SecurityUtils.getUserId());
        exception.setHandledAt(OffsetDateTime.now());
        purchaseExceptionMapper.updateById(exception);
        return toVO(exception);
    }

    private PurchaseExceptionVO toVO(PurchaseException exception) {
        PurchaseExceptionVO vo = new PurchaseExceptionVO();
        vo.setId(exception.getId());
        vo.setExceptionNo(exception.getExceptionNo());
        vo.setPurchaseOrderId(exception.getPurchaseOrderId());
        vo.setPurchaseOrderItemId(exception.getPurchaseOrderItemId());
        vo.setSupplierName(exception.getSupplierName());
        vo.setMaterialCode(exception.getMaterialCode());
        vo.setMaterialName(exception.getMaterialName());
        vo.setExceptionType(exception.getExceptionType());
        vo.setStatus(exception.getStatus());
        vo.setDescription(exception.getDescription());
        vo.setResolution(exception.getResolution());
        vo.setCreatedAt(exception.getCreatedAt());
        vo.setHandledAt(exception.getHandledAt());
        return vo;
    }
}
