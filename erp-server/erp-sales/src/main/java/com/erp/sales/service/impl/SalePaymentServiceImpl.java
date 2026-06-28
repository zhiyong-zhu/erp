package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.sales.domain.dto.SalePaymentRequest;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SalePayment;
import com.erp.sales.domain.vo.SalePaymentVO;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SalePaymentMapper;
import com.erp.sales.service.SalePaymentService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalePaymentServiceImpl implements SalePaymentService {

    private final SalePaymentMapper paymentMapper;
    private final SaleOrderMapper orderMapper;

    public SalePaymentServiceImpl(SalePaymentMapper paymentMapper, SaleOrderMapper orderMapper) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public SalePaymentVO createPayment(UUID saleOrderId, SalePaymentRequest request) {
        SaleOrder order = orderMapper.selectById(saleOrderId);
        if (order == null) {
            throw new BizException(10006, "销售单不存在");
        }

        BigDecimal paidSoFar = sumReceivedAmount(saleOrderId);
        BigDecimal receivable = order.getPayableAmount() == null ? BigDecimal.ZERO : order.getPayableAmount();
        if (paidSoFar.add(request.getReceivedAmount()).compareTo(receivable) > 0) {
            throw new BizException(10004, "收款金额超过未收金额（已收 " + paidSoFar + "，应收 " + receivable + "）");
        }

        SalePayment payment = new SalePayment();
        payment.setId(UUID.randomUUID());
        payment.setPaymentNo(generatePaymentNo());
        payment.setSaleOrderId(order.getId());
        payment.setSaleOrderNo(order.getOrderNo());
        payment.setCustomerId(order.getCustomerId());
        payment.setCustomerName(order.getCustomerName());
        payment.setReceivedAmount(request.getReceivedAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentTime(OffsetDateTime.now());
        payment.setRemark(request.getRemark());
        payment.setCreatedBy(SecurityUtils.getUserId());
        payment.setCreatedAt(OffsetDateTime.now());
        paymentMapper.insert(payment);

        BigDecimal newReceived = paidSoFar.add(request.getReceivedAmount());
        order.setPaidAmount(newReceived);
        if (newReceived.compareTo(receivable) >= 0) {
            order.setPaymentStatus("RECEIVED");
        } else if (newReceived.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentStatus("PARTIAL_RECEIVED");
        }
        order.setPaidAt(OffsetDateTime.now());
        orderMapper.updateById(order);

        return toVO(payment);
    }

    @Override
    public PageVO<SalePaymentVO> listPayments(long pageNum, long pageSize, UUID saleOrderId, UUID customerId) {
        Page<SalePayment> page = paymentMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SalePayment>()
                        .eq(saleOrderId != null, SalePayment::getSaleOrderId, saleOrderId)
                        .eq(customerId != null, SalePayment::getCustomerId, customerId)
                        .orderByDesc(SalePayment::getCreatedAt));
        var records = page.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private BigDecimal sumReceivedAmount(UUID saleOrderId) {
        return paymentMapper.selectList(
                        new LambdaQueryWrapper<SalePayment>()
                                .eq(SalePayment::getSaleOrderId, saleOrderId))
                .stream()
                .map(SalePayment::getReceivedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generatePaymentNo() {
        return "RCV-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private SalePaymentVO toVO(SalePayment payment) {
        SalePaymentVO vo = new SalePaymentVO();
        vo.setId(payment.getId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setSaleOrderId(payment.getSaleOrderId());
        vo.setSaleOrderNo(payment.getSaleOrderNo());
        vo.setCustomerId(payment.getCustomerId());
        vo.setCustomerName(payment.getCustomerName());
        vo.setReceivedAmount(payment.getReceivedAmount());
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setPaymentTime(payment.getPaymentTime());
        vo.setRemark(payment.getRemark());
        vo.setCreatedAt(payment.getCreatedAt());
        return vo;
    }
}
