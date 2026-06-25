package com.erp.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.entity.InventoryBalance;
import com.erp.inventory.mapper.InventoryBalanceMapper;
import com.erp.inventory.service.InventoryBalanceService;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryBalanceServiceImpl implements InventoryBalanceService {
    private final InventoryBalanceMapper inventoryBalanceMapper;
    private final MaterialMapper materialMapper;

    public InventoryBalanceServiceImpl(InventoryBalanceMapper inventoryBalanceMapper, MaterialMapper materialMapper) {
        this.inventoryBalanceMapper = inventoryBalanceMapper;
        this.materialMapper = materialMapper;
    }

    @Override
    @Transactional
    public InventoryBalance increase(Material material, BigDecimal quantity, InventoryPosition position) {
        validateMaterial(material);
        validatePositive(quantity, "入库数量必须大于0");
        InventoryBalance balance = getOrCreateBalance(material, position);
        BigDecimal nextQuantity = safe(balance.getAvailableQuantity()).add(quantity);
        updateBalance(balance, nextQuantity);
        updateMaterialStock(material, safe(material.getCurrentStock()).add(quantity));
        return balance;
    }

    @Override
    @Transactional
    public InventoryBalance decrease(Material material, BigDecimal quantity, InventoryPosition position) {
        validateMaterial(material);
        validatePositive(quantity, "出库数量必须大于0");
        InventoryBalance balance = getOrCreateBalance(material, position);
        BigDecimal currentQuantity = safe(balance.getAvailableQuantity());
        if (currentQuantity.compareTo(quantity) < 0) {
            throw new BizException(10004, "库存余额不足: " + material.getName());
        }
        OffsetDateTime now = OffsetDateTime.now();
        int updatedRows = inventoryBalanceMapper.decreaseAvailableIfEnough(balance.getId(), quantity, SecurityUtils.getUserId(), now);
        if (updatedRows == 0) {
            throw new BizException(10004, "库存余额不足或已被其他作业占用: " + material.getName());
        }
        balance.setAvailableQuantity(currentQuantity.subtract(quantity));
        balance.setTotalQuantity(balance.getAvailableQuantity().add(safe(balance.getFrozenQuantity())));
        balance.setUpdatedBy(SecurityUtils.getUserId());
        balance.setUpdatedAt(now);
        updateMaterialStock(material, safe(material.getCurrentStock()).subtract(quantity));
        return balance;
    }

    @Override
    @Transactional
    public InventoryBalance adjustTo(Material material, BigDecimal actualQuantity, InventoryPosition position) {
        validateMaterial(material);
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(10004, "实盘数量不能小于0");
        }
        InventoryBalance balance = getOrCreateBalance(material, position);
        BigDecimal currentQuantity = safe(balance.getAvailableQuantity());
        BigDecimal difference = actualQuantity.subtract(currentQuantity);
        updateBalance(balance, actualQuantity);
        updateMaterialStock(material, safe(material.getCurrentStock()).add(difference));
        return balance;
    }

    @Override
    @Transactional
    public InventoryBalance freezeForCheck(Material material, BigDecimal quantity, InventoryPosition position) {
        validateMaterial(material);
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(10004, "冻结数量不能小于0");
        }
        InventoryBalance balance = getOrCreateBalance(material, position);
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return balance;
        }
        OffsetDateTime now = OffsetDateTime.now();
        int updatedRows = inventoryBalanceMapper.freezeQuantity(balance.getId(), quantity, SecurityUtils.getUserId(), now);
        if (updatedRows == 0) {
            throw new BizException(10004, "盘点冻结库存不足或已被其他作业占用: " + material.getName());
        }
        balance.setAvailableQuantity(safe(balance.getAvailableQuantity()).subtract(quantity));
        balance.setFrozenQuantity(safe(balance.getFrozenQuantity()).add(quantity));
        balance.setTotalQuantity(safe(balance.getAvailableQuantity()).add(safe(balance.getFrozenQuantity())));
        balance.setUpdatedBy(SecurityUtils.getUserId());
        balance.setUpdatedAt(now);
        return balance;
    }

    @Override
    @Transactional
    public InventoryBalance approveCheckAdjustment(Material material, BigDecimal systemQuantity, BigDecimal actualQuantity, InventoryPosition position) {
        validateMaterial(material);
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(10004, "实盘数量不能小于0");
        }
        BigDecimal frozenQuantity = safe(systemQuantity);
        InventoryBalance balance = releaseCheckFreeze(material, frozenQuantity, position);
        BigDecimal currentQuantity = safe(balance.getAvailableQuantity());
        BigDecimal difference = actualQuantity.subtract(frozenQuantity);
        updateBalance(balance, currentQuantity.add(difference));
        updateMaterialStock(material, safe(material.getCurrentStock()).add(difference));
        return balance;
    }

    @Override
    @Transactional
    public InventoryBalance releaseCheckFreeze(Material material, BigDecimal quantity, InventoryPosition position) {
        validateMaterial(material);
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(10004, "释放冻结数量不能小于0");
        }
        InventoryBalance balance = getOrCreateBalance(material, position);
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return balance;
        }
        OffsetDateTime now = OffsetDateTime.now();
        int updatedRows = inventoryBalanceMapper.releaseFrozenQuantity(balance.getId(), quantity, SecurityUtils.getUserId(), now);
        if (updatedRows == 0) {
            throw new BizException(10004, "盘点冻结库存不足或已被处理: " + material.getName());
        }
        balance.setAvailableQuantity(safe(balance.getAvailableQuantity()).add(quantity));
        balance.setFrozenQuantity(safe(balance.getFrozenQuantity()).subtract(quantity));
        balance.setTotalQuantity(safe(balance.getAvailableQuantity()).add(safe(balance.getFrozenQuantity())));
        balance.setUpdatedBy(SecurityUtils.getUserId());
        balance.setUpdatedAt(now);
        return balance;
    }

    private InventoryBalance getOrCreateBalance(Material material, InventoryPosition rawPosition) {
        InventoryPosition position = (rawPosition == null ? InventoryPosition.defaults() : rawPosition).normalized();
        InventoryBalance balance = inventoryBalanceMapper.selectOne(new LambdaQueryWrapper<InventoryBalance>()
                .eq(InventoryBalance::getMaterialId, material.getId())
                .eq(InventoryBalance::getWarehouseCode, position.warehouseCode())
                .eq(InventoryBalance::getLocationCode, position.locationCode())
                .eq(InventoryBalance::getBatchNo, position.batchNo()));
        if (balance != null) {
            return balance;
        }

        OffsetDateTime now = OffsetDateTime.now();
        balance = new InventoryBalance();
        balance.setId(UUID.randomUUID());
        balance.setMaterialId(material.getId());
        balance.setMaterialCode(material.getCode());
        balance.setMaterialName(material.getName());
        balance.setWarehouseCode(position.warehouseCode());
        balance.setWarehouseName(position.warehouseName());
        balance.setLocationCode(position.locationCode());
        balance.setLocationName(position.locationName());
        balance.setBatchNo(position.batchNo());
        balance.setAvailableQuantity(BigDecimal.ZERO);
        balance.setFrozenQuantity(BigDecimal.ZERO);
        balance.setTotalQuantity(BigDecimal.ZERO);
        balance.setCreatedBy(SecurityUtils.getUserId());
        balance.setCreatedAt(now);
        balance.setUpdatedBy(SecurityUtils.getUserId());
        balance.setUpdatedAt(now);
        inventoryBalanceMapper.insert(balance);
        return balance;
    }

    private void updateBalance(InventoryBalance balance, BigDecimal availableQuantity) {
        balance.setAvailableQuantity(availableQuantity);
        balance.setTotalQuantity(availableQuantity.add(safe(balance.getFrozenQuantity())));
        balance.setUpdatedBy(SecurityUtils.getUserId());
        balance.setUpdatedAt(OffsetDateTime.now());
        inventoryBalanceMapper.updateById(balance);
    }

    private void updateMaterialStock(Material material, BigDecimal currentStock) {
        material.setCurrentStock(currentStock);
        material.setUpdatedBy(SecurityUtils.getUserId());
        material.setUpdatedAt(OffsetDateTime.now());
        materialMapper.updateById(material);
    }

    private void validateMaterial(Material material) {
        if (material == null || material.getId() == null) {
            throw new BizException(10006, "原料不存在");
        }
    }

    private void validatePositive(BigDecimal quantity, String message) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(10004, message);
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
