package com.erp.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.purchase.domain.dto.PurchasePaymentRequest;
import com.erp.purchase.domain.entity.PurchaseOrder;
import com.erp.purchase.domain.entity.PurchasePayment;
import com.erp.purchase.domain.vo.PurchasePaymentVO;
import com.erp.purchase.mapper.PurchaseOrderMapper;
import com.erp.purchase.mapper.PurchasePaymentMapper;
import com.erp.purchase.service.PurchasePaymentService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchasePaymentServiceImpl implements PurchasePaymentService {

    private final PurchasePaymentMapper paymentMapper;
    private final PurchaseOrderMapper orderMapper;

    public PurchasePaymentServiceImpl(PurchasePaymentMapper paymentMapper, PurchaseOrderMapper orderMapper) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public PurchasePaymentVO createPayment(UUID purchaseOrderId, PurchasePaymentRequest request) {
        PurchaseOrder order = orderMapper.selectById(purchaseOrderId);
        if (order == null) {
            throw new BizException(10006, "采购单不存在");
        }

        // 检查已付金额不超应付（已验收金额）
        BigDecimal paidSoFar = sumPaidAmount(purchaseOrderId);
        BigDecimal payable = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        if (paidSoFar.add(request.getPaidAmount()).compareTo(payable) > 0) {
            throw new BizException(10004, "付款金额超过未付金额（已付 " + paidSoFar + "，应付 " + payable + "）");
        }

        // 创建付款流水
        PurchasePayment payment = new PurchasePayment();
        payment.setId(UUID.randomUUID());
        payment.setPaymentNo(generatePaymentNo());
        payment.setPurchaseOrderId(order.getId());
        payment.setPurchaseOrderNo(order.getOrderNo());
        payment.setSupplierId(order.getSupplierId());
        payment.setSupplierName(order.getSupplierName());
        payment.setPaidAmount(request.getPaidAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentTime(OffsetDateTime.now());
        payment.setRemark(request.getRemark());
        payment.setCreatedBy(SecurityUtils.getUserId());
        payment.setCreatedAt(OffsetDateTime.now());
        paymentMapper.insert(payment);

        // 回写采购单已付金额和付款状态
        BigDecimal newPaid = paidSoFar.add(request.getPaidAmount());
        order.setPaidAmount(newPaid);
        if (newPaid.compareTo(payable) >= 0) {
            order.setPaymentStatus("PAID");
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentStatus("PARTIAL_PAID");
        }
        orderMapper.updateById(order);

        return toVO(payment);
    }

    @Override
    public PageVO<PurchasePaymentVO> listPayments(long pageNum, long pageSize, UUID purchaseOrderId, UUID supplierId) {
        Page<PurchasePayment> page = paymentMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<PurchasePayment>()
                        .eq(purchaseOrderId != null, PurchasePayment::getPurchaseOrderId, purchaseOrderId)
                        .eq(supplierId != null, PurchasePayment::getSupplierId, supplierId)
                        .orderByDesc(PurchasePayment::getCreatedAt));
        var records = page.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private BigDecimal sumPaidAmount(UUID purchaseOrderId) {
        return paymentMapper.selectList(
                        new LambdaQueryWrapper<PurchasePayment>()
                                .eq(PurchasePayment::getPurchaseOrderId, purchaseOrderId))
                .stream()
                .map(PurchasePayment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generatePaymentNo() {
        return "PAY-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private PurchasePaymentVO toVO(PurchasePayment payment) {
        PurchasePaymentVO vo = new PurchasePaymentVO();
        vo.setId(payment.getId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setPurchaseOrderId(payment.getPurchaseOrderId());
        vo.setPurchaseOrderNo(payment.getPurchaseOrderNo());
        vo.setSupplierId(payment.getSupplierId());
        vo.setSupplierName(payment.getSupplierName());
        vo.setPaidAmount(payment.getPaidAmount());
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setPaymentTime(payment.getPaymentTime());
        vo.setRemark(payment.getRemark());
        vo.setCreatedAt(payment.getCreatedAt());
        return vo;
    }
}
