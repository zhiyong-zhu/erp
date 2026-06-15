package com.erp.sales.service;

import java.util.UUID;

public interface DeliveryNoteService {
    byte[] generatePdf(UUID saleOrderId);
}
