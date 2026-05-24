package com.erp.material.service;

import com.erp.material.domain.entity.SupplierQuote;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SupplierQuoteQueryService {
    List<SupplierQuote> listEffectiveQuotesByMaterialId(UUID materialId, LocalDate referenceDate);
}
