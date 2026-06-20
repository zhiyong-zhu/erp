package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.vo.SaleReportVO;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SaleReturnMapper;
import com.erp.sales.service.SaleReportService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SaleReportServiceImpl implements SaleReportService {
    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final SaleReturnMapper saleReturnMapper;

    public SaleReportServiceImpl(
            SaleOrderMapper saleOrderMapper,
            SaleOrderItemMapper saleOrderItemMapper,
            SaleReturnMapper saleReturnMapper
    ) {
        this.saleOrderMapper = saleOrderMapper;
        this.saleOrderItemMapper = saleOrderItemMapper;
        this.saleReturnMapper = saleReturnMapper;
    }

    @Override
    public SaleReportVO summary() {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .notIn(SaleOrder::getStatus, List.of("PENDING_CONFIRM", "CANCELLED"))
        );
        List<SaleReturn> returns = saleReturnMapper.selectList(
                new LambdaQueryWrapper<SaleReturn>()
                        .in(SaleReturn::getStatus, List.of("REFUNDED", "COMPLETED"))
        );

        BigDecimal orderAmount = orders.stream()
                .map(SaleOrder::getPayableAmount)
                .reduce(BigDecimal.ZERO, this::add);
        BigDecimal returnAmount = returns.stream()
                .map(SaleReturn::getTotalAmount)
                .reduce(BigDecimal.ZERO, this::add);

        SaleReportVO report = new SaleReportVO();
        report.setOrderAmount(orderAmount);
        report.setReturnAmount(returnAmount);
        report.setNetSalesAmount(orderAmount.subtract(returnAmount));
        report.setOrderCount((long) orders.size());
        report.setReturnCount((long) returns.size());
        report.setCustomerCount(orders.stream().map(SaleOrder::getCustomerId).filter(id -> id != null).distinct().count());
        report.setTopCustomers(buildCustomerRanks(orders, returns));
        report.setTopProducts(buildProductRanks(orders));
        return report;
    }

    private List<SaleReportVO.CustomerRank> buildCustomerRanks(List<SaleOrder> orders, List<SaleReturn> returns) {
        Map<UUID, List<SaleOrder>> ordersByCustomer = orders.stream()
                .filter(order -> order.getCustomerId() != null)
                .collect(Collectors.groupingBy(SaleOrder::getCustomerId));
        Map<UUID, List<SaleReturn>> returnsByCustomer = returns.stream()
                .filter(saleReturn -> saleReturn.getCustomerId() != null)
                .collect(Collectors.groupingBy(SaleReturn::getCustomerId));

        return ordersByCustomer.entrySet().stream()
                .map(entry -> {
                    UUID customerId = entry.getKey();
                    List<SaleOrder> customerOrders = entry.getValue();
                    BigDecimal orderAmount = customerOrders.stream()
                            .map(SaleOrder::getPayableAmount)
                            .reduce(BigDecimal.ZERO, this::add);
                    BigDecimal returnAmount = returnsByCustomer.getOrDefault(customerId, List.of()).stream()
                            .map(SaleReturn::getTotalAmount)
                            .reduce(BigDecimal.ZERO, this::add);
                    SaleReportVO.CustomerRank rank = new SaleReportVO.CustomerRank();
                    rank.setCustomerName(customerOrders.get(0).getCustomerName());
                    rank.setOrderAmount(orderAmount);
                    rank.setReturnAmount(returnAmount);
                    rank.setNetSalesAmount(orderAmount.subtract(returnAmount));
                    rank.setOrderCount((long) customerOrders.size());
                    return rank;
                })
                .sorted(Comparator.comparing(SaleReportVO.CustomerRank::getNetSalesAmount).reversed())
                .limit(10)
                .toList();
    }

    private List<SaleReportVO.ProductRank> buildProductRanks(List<SaleOrder> orders) {
        List<UUID> orderIds = orders.stream().map(SaleOrder::getId).toList();
        if (orderIds.isEmpty()) {
            return List.of();
        }
        List<SaleOrderItem> items = saleOrderItemMapper.selectList(
                new LambdaQueryWrapper<SaleOrderItem>().in(SaleOrderItem::getSaleOrderId, orderIds)
        );
        return items.stream()
                .collect(Collectors.groupingBy(this::productKey))
                .values()
                .stream()
                .map(group -> {
                    SaleOrderItem first = group.get(0);
                    SaleReportVO.ProductRank rank = new SaleReportVO.ProductRank();
                    rank.setSkuCode(first.getSkuCode());
                    rank.setProductName(first.getProductName());
                    rank.setQuantity(group.stream().map(SaleOrderItem::getQuantity).reduce(BigDecimal.ZERO, this::add));
                    rank.setSalesAmount(group.stream().map(SaleOrderItem::getAmount).reduce(BigDecimal.ZERO, this::add));
                    return rank;
                })
                .sorted(Comparator.comparing(SaleReportVO.ProductRank::getSalesAmount).reversed())
                .limit(10)
                .toList();
    }

    private String productKey(SaleOrderItem item) {
        if (item.getSkuCode() != null && !item.getSkuCode().isBlank()) {
            return item.getSkuCode();
        }
        if (item.getSkuId() != null) {
            return item.getSkuId().toString();
        }
        return item.getProductName() == null ? "UNKNOWN" : item.getProductName();
    }

    private BigDecimal add(BigDecimal left, BigDecimal right) {
        return (left == null ? BigDecimal.ZERO : left).add(right == null ? BigDecimal.ZERO : right);
    }
}
