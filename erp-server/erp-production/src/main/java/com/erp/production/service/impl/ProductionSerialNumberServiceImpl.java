package com.erp.production.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.production.domain.entity.SerialNumber;
import com.erp.production.mapper.SerialNumberMapper;
import com.erp.production.service.ProductionSerialNumberService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionSerialNumberServiceImpl implements ProductionSerialNumberService {
    private static final String GENERATED = "GENERATED";
    private static final String PACKED = "PACKED";
    private static final String STOCKED = "STOCKED";
    private static final String SHIPPED = "SHIPPED";

    private final SerialNumberMapper serialNumberMapper;

    public ProductionSerialNumberServiceImpl(SerialNumberMapper serialNumberMapper) {
        this.serialNumberMapper = serialNumberMapper;
    }

    @Override
    @Transactional
    public void markPacked(UUID batchId, UUID productId, List<String> serialNos, OffsetDateTime packedAt) {
        List<String> normalized = normalizeSerialNos(serialNos);
        if (normalized.isEmpty()) {
            return;
        }
        OffsetDateTime now = packedAt == null ? OffsetDateTime.now() : packedAt;
        List<SerialNumber> serialNumbers = findSerialNumbers(normalized);
        if (serialNumbers.size() != normalized.size()) {
            throw new BizException(10004, "存在未生成的序列号，不能装箱");
        }
        for (SerialNumber serialNumber : serialNumbers) {
            if (!batchId.equals(serialNumber.getBatchId()) || !productId.equals(serialNumber.getProductId())) {
                throw new BizException(10004, "序列号不属于当前生产批次或产品: " + serialNumber.getSerialNo());
            }
            if (!GENERATED.equals(serialNumber.getStatus())) {
                throw new BizException(10004, "只有已生成序列号允许装箱: " + serialNumber.getSerialNo());
            }
            serialNumber.setStatus(PACKED);
            serialNumber.setProducedAt(now);
            serialNumber.setUpdatedBy(SecurityUtils.getUserId());
            serialNumber.setUpdatedAt(now);
            serialNumberMapper.updateById(serialNumber);
        }
    }

    @Override
    @Transactional
    public void markBatchStocked(UUID batchId, BigDecimal receiptQuantity, OffsetDateTime stockedAt) {
        List<SerialNumber> serialNumbers = serialNumberMapper.selectList(
                new LambdaQueryWrapper<SerialNumber>().eq(SerialNumber::getBatchId, batchId)
        );
        if (serialNumbers == null || serialNumbers.isEmpty()) {
            return;
        }
        int expectedQuantity = exactIntegerQuantity(receiptQuantity);
        List<SerialNumber> packedSerials = serialNumbers.stream()
                .filter(serialNumber -> PACKED.equals(serialNumber.getStatus()))
                .toList();
        if (packedSerials.size() != expectedQuantity || packedSerials.size() != serialNumbers.size()) {
            throw new BizException(10004, "序列号装箱数量与完工入库数量不一致");
        }
        OffsetDateTime now = stockedAt == null ? OffsetDateTime.now() : stockedAt;
        for (SerialNumber serialNumber : packedSerials) {
            serialNumber.setStatus(STOCKED);
            serialNumber.setUpdatedBy(SecurityUtils.getUserId());
            serialNumber.setUpdatedAt(now);
            serialNumberMapper.updateById(serialNumber);
        }
    }

    @Override
    @Transactional
    public void markShipped(UUID productId, List<String> serialNos, OffsetDateTime shippedAt) {
        List<String> normalized = normalizeSerialNos(serialNos);
        if (normalized.isEmpty()) {
            return;
        }
        OffsetDateTime now = shippedAt == null ? OffsetDateTime.now() : shippedAt;
        List<SerialNumber> serialNumbers = findSerialNumbers(normalized);
        if (serialNumbers.size() != normalized.size()) {
            throw new BizException(10004, "存在不存在的序列号，不能发货");
        }
        for (SerialNumber serialNumber : serialNumbers) {
            if (!productId.equals(serialNumber.getProductId())) {
                throw new BizException(10004, "序列号不属于当前发货产品: " + serialNumber.getSerialNo());
            }
            if (!STOCKED.equals(serialNumber.getStatus())) {
                throw new BizException(10004, "只有已入库序列号允许发货: " + serialNumber.getSerialNo());
            }
            serialNumber.setStatus(SHIPPED);
            serialNumber.setShippedAt(now);
            serialNumber.setUpdatedBy(SecurityUtils.getUserId());
            serialNumber.setUpdatedAt(now);
            serialNumberMapper.updateById(serialNumber);
        }
    }

    private List<SerialNumber> findSerialNumbers(List<String> serialNos) {
        return serialNumberMapper.selectList(new LambdaQueryWrapper<SerialNumber>().in(SerialNumber::getSerialNo, serialNos));
    }

    private List<String> normalizeSerialNos(List<String> serialNos) {
        if (serialNos == null || serialNos.isEmpty()) {
            return List.of();
        }
        Set<String> uniqueSerialNos = new LinkedHashSet<>();
        for (String serialNo : serialNos) {
            if (serialNo != null && !serialNo.isBlank()) {
                uniqueSerialNos.add(serialNo.trim());
            }
        }
        if (uniqueSerialNos.size() != serialNos.stream().filter(serialNo -> serialNo != null && !serialNo.isBlank()).count()) {
            throw new BizException(10004, "序列号不能重复");
        }
        return List.copyOf(uniqueSerialNos);
    }

    private int exactIntegerQuantity(BigDecimal quantity) {
        try {
            return quantity == null ? 0 : quantity.stripTrailingZeros().intValueExact();
        } catch (ArithmeticException ex) {
            throw new BizException(10004, "启用序列号后，完工入库数量必须为整数");
        }
    }
}
