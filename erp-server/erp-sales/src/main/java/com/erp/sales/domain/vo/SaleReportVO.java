package com.erp.sales.domain.vo;

import java.math.BigDecimal;
import java.util.List;

public class SaleReportVO {
    private BigDecimal orderAmount;
    private BigDecimal returnAmount;
    private BigDecimal netSalesAmount;
    private Long orderCount;
    private Long returnCount;
    private Long customerCount;
    private List<CustomerRank> topCustomers;
    private List<ProductRank> topProducts;

    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
    public BigDecimal getNetSalesAmount() { return netSalesAmount; }
    public void setNetSalesAmount(BigDecimal netSalesAmount) { this.netSalesAmount = netSalesAmount; }
    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
    public Long getReturnCount() { return returnCount; }
    public void setReturnCount(Long returnCount) { this.returnCount = returnCount; }
    public Long getCustomerCount() { return customerCount; }
    public void setCustomerCount(Long customerCount) { this.customerCount = customerCount; }
    public List<CustomerRank> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<CustomerRank> topCustomers) { this.topCustomers = topCustomers; }
    public List<ProductRank> getTopProducts() { return topProducts; }
    public void setTopProducts(List<ProductRank> topProducts) { this.topProducts = topProducts; }

    public static class CustomerRank {
        private String customerName;
        private BigDecimal orderAmount;
        private BigDecimal returnAmount;
        private BigDecimal netSalesAmount;
        private Long orderCount;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public BigDecimal getOrderAmount() { return orderAmount; }
        public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
        public BigDecimal getReturnAmount() { return returnAmount; }
        public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
        public BigDecimal getNetSalesAmount() { return netSalesAmount; }
        public void setNetSalesAmount(BigDecimal netSalesAmount) { this.netSalesAmount = netSalesAmount; }
        public Long getOrderCount() { return orderCount; }
        public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
    }

    public static class ProductRank {
        private String skuCode;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal salesAmount;

        public String getSkuCode() { return skuCode; }
        public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getSalesAmount() { return salesAmount; }
        public void setSalesAmount(BigDecimal salesAmount) { this.salesAmount = salesAmount; }
    }
}
