package com.erp.sales.service.impl;

import com.erp.common.core.exception.BizException;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.ShippingOrder;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.ShippingOrderMapper;
import com.erp.sales.service.DeliveryNoteService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeliveryNoteServiceImpl implements DeliveryNoteService {

    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final ShippingOrderMapper shippingOrderMapper;

    public DeliveryNoteServiceImpl(
            SaleOrderMapper saleOrderMapper,
            SaleOrderItemMapper saleOrderItemMapper,
            ShippingOrderMapper shippingOrderMapper
    ) {
        this.saleOrderMapper = saleOrderMapper;
        this.saleOrderItemMapper = saleOrderItemMapper;
        this.shippingOrderMapper = shippingOrderMapper;
    }

    @Override
    public byte[] generatePdf(UUID saleOrderId) {
        SaleOrder order = saleOrderMapper.selectById(saleOrderId);
        if (order == null) {
            throw new BizException(10006, "销售订单不存在");
        }
        List<SaleOrderItem> items = saleOrderItemMapper.selectBySaleOrderId(order.getId());
        List<ShippingOrder> shippings = shippingOrderMapper.selectBySaleOrderId(order.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(new Rectangle(595, 842));
            PdfWriter.getInstance(document, baos);
            document.open();

            // Chinese font - use built-in Helvetica as fallback, but text will be readable
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10);
            Font smallFont = new Font(Font.HELVETICA, 9);

            // Title
            Paragraph title = new Paragraph("DELIVERY NOTE / CHU KU DAN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Order info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 2});
            addInfoRow(infoTable, "Order No / DAN HAO:", order.getOrderNo(), normalFont);
            addInfoRow(infoTable, "Customer / KE HU:", order.getCustomerName(), normalFont);
            addInfoRow(infoTable, "Source / LAI YUAN:", order.getOrderSource(), normalFont);
            addInfoRow(infoTable, "Status / ZHUANG TAI:", order.getStatus(), normalFont);
            if (order.getOrderedAt() != null) {
                addInfoRow(infoTable, "Date / RI QI:", order.getOrderedAt().toString(), normalFont);
            }
            if (order.getRemark() != null && !order.getRemark().isBlank()) {
                addInfoRow(infoTable, "Remark / BEI ZHU:", order.getRemark(), normalFont);
            }
            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Shipping info
            if (!shippings.isEmpty()) {
                ShippingOrder shipping = shippings.get(0);
                PdfPTable shipTable = new PdfPTable(2);
                shipTable.setWidthPercentage(100);
                shipTable.setWidths(new float[]{1, 2});
                addInfoRow(shipTable, "Carrier / CHENG YUN SHANG:", shipping.getCarrierName() != null ? shipping.getCarrierName() : "-", normalFont);
                addInfoRow(shipTable, "Tracking / YUN DAN HAO:", shipping.getTrackingNumber() != null ? shipping.getTrackingNumber() : "-", normalFont);
                document.add(shipTable);
                document.add(new Paragraph(" "));
            }

            // Items table
            PdfPTable itemTable = new PdfPTable(5);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{3, 3, 1, 2, 2});

            // Header row
            String[] headers = {"SKU Code", "Product Name", "Unit", "Quantity", "Amount"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(240, 240, 240));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                itemTable.addCell(cell);
            }

            // Data rows
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (SaleOrderItem item : items) {
                itemTable.addCell(new Phrase(safe(item.getSkuCode()), smallFont));
                itemTable.addCell(new Phrase(safe(item.getProductName()), smallFont));
                itemTable.addCell(new Phrase(safe(item.getUnit()), smallFont));
                PdfPCell qtyCell = new PdfPCell(new Phrase(safe(item.getQuantity()), smallFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemTable.addCell(qtyCell);
                PdfPCell amtCell = new PdfPCell(new Phrase(safe(item.getAmount()), smallFont));
                amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                itemTable.addCell(amtCell);
                if (item.getAmount() != null) {
                    totalAmount = totalAmount.add(item.getAmount());
                }
            }
            document.add(itemTable);

            // Totals
            document.add(new Paragraph(" "));
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setWidths(new float[]{1, 1});
            addInfoRow(totalTable, "Total Amount:", "¥" + totalAmount.toPlainString(), normalFont);
            if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                addInfoRow(totalTable, "Discount:", "-¥" + order.getDiscountAmount().toPlainString(), normalFont);
            }
            if (order.getFreightAmount() != null && order.getFreightAmount().compareTo(BigDecimal.ZERO) > 0) {
                addInfoRow(totalTable, "Freight:", "¥" + order.getFreightAmount().toPlainString(), normalFont);
            }
            addInfoRow(totalTable, "Payable:", "¥" + (order.getPayableAmount() != null ? order.getPayableAmount().toPlainString() : totalAmount.toPlainString()), headerFont);
            document.add(totalTable);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Printed: " + java.time.OffsetDateTime.now().toString(), smallFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BizException(10006, "PDF generation failed: " + e.getMessage());
        }
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(3);
        table.addCell(valueCell);
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "-";
    }
}
