package com.erp.sales.integration;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface EcommercePlatformService {
    String getPlatformCode();
    List<PlatformOrder> pullOrders(UUID shopId, OffsetDateTime startTime, OffsetDateTime endTime);
}
