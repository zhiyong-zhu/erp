package com.erp.production.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductionSerialNumberService {
    void markPacked(UUID batchId, UUID productId, List<String> serialNos, OffsetDateTime packedAt);
    void markBatchStocked(UUID batchId, BigDecimal receiptQuantity, OffsetDateTime stockedAt);
    void markShipped(UUID productId, List<String> serialNos, OffsetDateTime shippedAt);
}
