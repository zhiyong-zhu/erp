package com.erp.common.report.excel;

import com.erp.common.core.exception.BizException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelExportUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExcelExportUtils() {
    }

    public static <T> ByteArrayInputStream export(String sheetName, List<Column<T>> columns, List<T> rows, String errorMessage) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row header = sheet.createRow(0);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                header.createCell(columnIndex).setCellValue(columns.get(columnIndex).header());
            }

            int rowIndex = 1;
            for (T item : rows) {
                Row row = sheet.createRow(rowIndex++);
                for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                    Object value = columns.get(columnIndex).valueGetter().apply(item);
                    writeCell(row.createCell(columnIndex), value);
                }
            }

            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                sheet.autoSizeColumn(columnIndex);
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception ex) {
            throw new BizException(10006, errorMessage);
        }
    }

    public static <T> Column<T> column(String header, Function<T, Object> valueGetter) {
        return new Column<>(header, valueGetter);
    }

    private static void writeCell(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        if (value instanceof BigDecimal number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
            return;
        }
        if (value instanceof OffsetDateTime dateTime) {
            cell.setCellValue(dateTime.format(DATE_TIME_FORMATTER));
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }

    public record Column<T>(String header, Function<T, Object> valueGetter) {
    }
}
