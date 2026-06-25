package com.erp.production.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.common.core.exception.BizException;
import com.erp.production.domain.entity.SerialNumber;
import com.erp.production.mapper.SerialNumberMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionSerialNumberServiceImplTest {
    @Mock private SerialNumberMapper serialNumberMapper;

    private ProductionSerialNumberServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductionSerialNumberServiceImpl(serialNumberMapper);
    }

    @Test
    void markPackedOnlyAllowsGeneratedSerialsFromSameBatchAndProduct() {
        UUID batchId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        SerialNumber serialNumber = serialNumber("SN-001", batchId, productId, "GENERATED");
        when(serialNumberMapper.selectList(any(Wrapper.class))).thenReturn(List.of(serialNumber));

        service.markPacked(batchId, productId, List.of("SN-001"), OffsetDateTime.now());

        verify(serialNumberMapper).updateById(serialNumber);
    }

    @Test
    void markPackedRejectsAlreadyPackedSerials() {
        UUID batchId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        SerialNumber serialNumber = serialNumber("SN-001", batchId, productId, "PACKED");
        when(serialNumberMapper.selectList(any(Wrapper.class))).thenReturn(List.of(serialNumber));

        assertThrows(BizException.class, () -> service.markPacked(batchId, productId, List.of("SN-001"), OffsetDateTime.now()));

        verify(serialNumberMapper, never()).updateById(any(SerialNumber.class));
    }

    @Test
    void markBatchStockedRejectsWhenPackedQuantityDoesNotMatchReceiptQuantity() {
        UUID batchId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(serialNumberMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                serialNumber("SN-001", batchId, productId, "PACKED"),
                serialNumber("SN-002", batchId, productId, "GENERATED")
        ));

        assertThrows(BizException.class, () -> service.markBatchStocked(batchId, new BigDecimal("2.00"), OffsetDateTime.now()));

        verify(serialNumberMapper, never()).updateById(any(SerialNumber.class));
    }

    @Test
    void markShippedOnlyAllowsStockedSerials() {
        UUID productId = UUID.randomUUID();
        SerialNumber serialNumber = serialNumber("SN-001", UUID.randomUUID(), productId, "STOCKED");
        when(serialNumberMapper.selectList(any(Wrapper.class))).thenReturn(List.of(serialNumber));

        service.markShipped(productId, List.of("SN-001"), OffsetDateTime.now());

        verify(serialNumberMapper).updateById(serialNumber);
    }

    private SerialNumber serialNumber(String serialNo, UUID batchId, UUID productId, String status) {
        SerialNumber serialNumber = new SerialNumber();
        serialNumber.setId(UUID.randomUUID());
        serialNumber.setSerialNo(serialNo);
        serialNumber.setBatchId(batchId);
        serialNumber.setProductId(productId);
        serialNumber.setStatus(status);
        return serialNumber;
    }
}
