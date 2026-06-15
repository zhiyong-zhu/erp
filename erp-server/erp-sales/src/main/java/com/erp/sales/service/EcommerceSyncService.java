package com.erp.sales.service;

import java.util.UUID;

public interface EcommerceSyncService {
    int syncOrdersFromShop(UUID shopId);
}
