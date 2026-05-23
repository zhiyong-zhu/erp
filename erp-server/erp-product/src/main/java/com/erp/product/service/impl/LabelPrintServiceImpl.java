package com.erp.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.product.domain.dto.LabelPrintItemRequest;
import com.erp.product.domain.dto.LabelPrintRequest;
import com.erp.product.domain.entity.LabelTemplate;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductPackage;
import com.erp.product.domain.entity.ProductSku;
import com.erp.product.domain.vo.LabelPrintPreviewVO;
import com.erp.product.mapper.LabelTemplateMapper;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.mapper.ProductPackageMapper;
import com.erp.product.mapper.ProductSkuMapper;
import com.erp.product.service.LabelPrintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LabelPrintServiceImpl implements LabelPrintService {
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductPackageMapper productPackageMapper;
    private final LabelTemplateMapper labelTemplateMapper;
    private final ObjectMapper objectMapper;

    public LabelPrintServiceImpl(ProductMapper productMapper,
                                 ProductSkuMapper productSkuMapper,
                                 ProductPackageMapper productPackageMapper,
                                 LabelTemplateMapper labelTemplateMapper,
                                 ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.productPackageMapper = productPackageMapper;
        this.labelTemplateMapper = labelTemplateMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public LabelPrintPreviewVO preview(LabelPrintRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(10004, "至少需要一条打印项");
        }
        int totalCount = request.getItems() == null ? 0 : request.getItems().stream().mapToInt(LabelPrintItemRequest::getQuantity).sum();
        String summary = request.getItems() == null
                ? "无打印项"
                : request.getItems().stream()
                    .map(item -> "SKU " + item.getSkuId() + " / 包装层级 " + item.getPackageLevel() + " / 数量 " + item.getQuantity())
                    .collect(Collectors.joining(" ; "));
        String previewHtml = buildPreviewHtml(request);
        LabelPrintPreviewVO vo = new LabelPrintPreviewVO();
        vo.setPdfUrl("/mock/labels/preview/" + System.currentTimeMillis() + ".pdf");
        vo.setTotalCount(totalCount);
        vo.setSummary(summary);
        vo.setPreviewHtml(previewHtml);
        return vo;
    }

    private String buildPreviewHtml(LabelPrintRequest request) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='display:flex;flex-wrap:wrap;gap:12px;'>");
        for (LabelPrintItemRequest item : request.getItems()) {
            ProductSku sku = productSkuMapper.selectById(item.getSkuId());
            if (sku == null) {
                throw new BizException(10006, "SKU不存在");
            }
            Product product = productMapper.selectById(sku.getProductId());
            ProductPackage productPackage = productPackageMapper.selectOne(new LambdaQueryWrapper<ProductPackage>()
                    .eq(ProductPackage::getProductId, sku.getProductId())
                    .eq(ProductPackage::getLevel, item.getPackageLevel()));
            LabelTemplate template = labelTemplateMapper.selectById(item.getLabelTemplateId());
            if (product == null || productPackage == null || template == null) {
                throw new BizException(10006, "标签打印依赖数据不存在");
            }
            Map<String, String> context = buildPrintContext(product, sku, productPackage);
            html.append(renderTemplateCard(template, context, item.getQuantity()));
        }
        html.append("</div>");
        return html.toString();
    }

    private Map<String, String> buildPrintContext(Product product, ProductSku sku, ProductPackage productPackage) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("productName", product.getName());
        context.put("productCode", product.getCode());
        context.put("skuCode", sku.getSkuCode());
        context.put("skuAttributes", joinAttributes(sku.getAttributes()));
        context.put("barcode", productPackage.getBarcode() != null ? productPackage.getBarcode() : (sku.getBarcode() == null ? "" : sku.getBarcode()));
        context.put("packageName", productPackage.getName());
        context.put("packageLevel", String.valueOf(productPackage.getLevel()));
        context.put("packageQuantity", String.valueOf(productPackage.getQuantity()));
        context.put("weight", productPackage.getWeight() == null ? "" : productPackage.getWeight().toPlainString());
        context.put("dimensions", formatDimensions(productPackage.getDimensions()));
        context.put("printDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        return context;
    }

    private String renderTemplateCard(LabelTemplate template, Map<String, String> context, int quantity) {
        String templateName = template.getName();
        String productName = context.get("productName");
        String skuCode = context.get("skuCode");
        String skuAttributes = context.get("skuAttributes");
        String barcode = context.get("barcode");
        return """
                <div style='width:300px;border:1px solid #d9e3ea;border-radius:12px;background:#ffffff;padding:12px;box-shadow:0 4px 12px rgba(15,23,42,0.06);'>
                  <div style='font-size:12px;color:#64748b;margin-bottom:8px;'>模板：%s · 数量：%s</div>
                  <div style='font-size:18px;font-weight:700;color:#0f172a;'>%s</div>
                  <div style='font-size:13px;color:#334155;margin-top:6px;'>SKU：%s</div>
                  <div style='font-size:13px;color:#334155;margin-top:4px;'>规格：%s</div>
                  <div style='margin-top:12px;padding:10px;border:1px dashed #94a3b8;text-align:center;font-family:monospace;'>%s</div>
                </div>
                """.formatted(templateName, quantity, productName, skuCode, skuAttributes, barcode);
    }

    private String joinAttributes(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        try {
            Map<String, Object> attributes = objectMapper.readValue(raw, Map.class);
            return attributes.values().stream().map(String::valueOf).collect(Collectors.joining("/"));
        } catch (Exception ex) {
            return raw;
        }
    }

    private String formatDimensions(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        try {
            Map<String, Object> dimensions = objectMapper.readValue(raw, Map.class);
            return "%s×%s×%s%s".formatted(
                    dimensions.getOrDefault("length", "-"),
                    dimensions.getOrDefault("width", "-"),
                    dimensions.getOrDefault("height", "-"),
                    dimensions.getOrDefault("unit", "cm")
            );
        } catch (Exception ex) {
            return raw;
        }
    }
}
