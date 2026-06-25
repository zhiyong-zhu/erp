package com.erp.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.dto.InventoryLocationRequest;
import com.erp.inventory.domain.dto.InventoryWarehouseRequest;
import com.erp.inventory.domain.entity.InventoryLocation;
import com.erp.inventory.domain.entity.InventoryWarehouse;
import com.erp.inventory.domain.vo.InventoryLocationVO;
import com.erp.inventory.domain.vo.InventoryWarehouseVO;
import com.erp.inventory.mapper.InventoryLocationMapper;
import com.erp.inventory.mapper.InventoryWarehouseMapper;
import com.erp.inventory.service.InventoryWarehouseService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryWarehouseServiceImpl implements InventoryWarehouseService {
    private final InventoryWarehouseMapper warehouseMapper;
    private final InventoryLocationMapper locationMapper;

    public InventoryWarehouseServiceImpl(InventoryWarehouseMapper warehouseMapper, InventoryLocationMapper locationMapper) {
        this.warehouseMapper = warehouseMapper;
        this.locationMapper = locationMapper;
    }

    @Override
    public PageVO<InventoryWarehouseVO> listWarehouses(long pageNum, long pageSize, String keyword, Integer status) {
        Page<InventoryWarehouse> page = warehouseMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryWarehouse>()
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(InventoryWarehouse::getCode, keyword)
                                .or()
                                .like(InventoryWarehouse::getName, keyword))
                        .eq(status != null, InventoryWarehouse::getStatus, status)
                        .orderByAsc(InventoryWarehouse::getSortOrder)
                        .orderByDesc(InventoryWarehouse::getCreatedAt)
        );
        List<InventoryWarehouseVO> records = page.getRecords().stream().map(this::toWarehouseVO).toList();
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public InventoryWarehouseVO saveWarehouse(UUID id, InventoryWarehouseRequest request) {
        validateWarehouseRequest(request);
        String code = request.getCode().trim();
        InventoryWarehouse warehouse = id == null ? new InventoryWarehouse() : warehouseMapper.selectById(id);
        if (id != null && warehouse == null) {
            throw new BizException(10006, "仓库不存在");
        }
        InventoryWarehouse sameCode = warehouseMapper.selectOne(new LambdaQueryWrapper<InventoryWarehouse>().eq(InventoryWarehouse::getCode, code));
        if (sameCode != null && (id == null || !sameCode.getId().equals(id))) {
            throw new BizException(10004, "仓库编码已存在");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (warehouse.getId() == null) {
            warehouse.setId(UUID.randomUUID());
            warehouse.setCreatedBy(SecurityUtils.getUserId());
            warehouse.setCreatedAt(now);
        }
        warehouse.setCode(code);
        warehouse.setName(request.getName().trim());
        warehouse.setAddress(trimToNull(request.getAddress()));
        warehouse.setManagerName(trimToNull(request.getManagerName()));
        warehouse.setPhone(trimToNull(request.getPhone()));
        warehouse.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        warehouse.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        warehouse.setRemark(trimToNull(request.getRemark()));
        warehouse.setUpdatedBy(SecurityUtils.getUserId());
        warehouse.setUpdatedAt(now);
        if (id == null) {
            warehouseMapper.insert(warehouse);
        } else {
            warehouseMapper.updateById(warehouse);
            syncLocationWarehouseSnapshot(warehouse);
        }
        return toWarehouseVO(warehouse);
    }

    @Override
    public PageVO<InventoryLocationVO> listLocations(long pageNum, long pageSize, UUID warehouseId, String keyword, Integer status) {
        Page<InventoryLocation> page = locationMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryLocation>()
                        .eq(warehouseId != null, InventoryLocation::getWarehouseId, warehouseId)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(InventoryLocation::getCode, keyword)
                                .or()
                                .like(InventoryLocation::getName, keyword)
                                .or()
                                .like(InventoryLocation::getWarehouseName, keyword))
                        .eq(status != null, InventoryLocation::getStatus, status)
                        .orderByAsc(InventoryLocation::getWarehouseCode)
                        .orderByAsc(InventoryLocation::getSortOrder)
                        .orderByDesc(InventoryLocation::getCreatedAt)
        );
        List<InventoryLocationVO> records = page.getRecords().stream().map(this::toLocationVO).toList();
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public InventoryLocationVO saveLocation(UUID id, InventoryLocationRequest request) {
        validateLocationRequest(request);
        InventoryWarehouse warehouse = warehouseMapper.selectById(request.getWarehouseId());
        if (warehouse == null) {
            throw new BizException(10006, "仓库不存在");
        }
        String code = request.getCode().trim();
        InventoryLocation location = id == null ? new InventoryLocation() : locationMapper.selectById(id);
        if (id != null && location == null) {
            throw new BizException(10006, "库位不存在");
        }
        InventoryLocation sameCode = locationMapper.selectOne(new LambdaQueryWrapper<InventoryLocation>()
                .eq(InventoryLocation::getWarehouseId, warehouse.getId())
                .eq(InventoryLocation::getCode, code));
        if (sameCode != null && (id == null || !sameCode.getId().equals(id))) {
            throw new BizException(10004, "同一仓库下库位编码已存在");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (location.getId() == null) {
            location.setId(UUID.randomUUID());
            location.setCreatedBy(SecurityUtils.getUserId());
            location.setCreatedAt(now);
        }
        location.setWarehouseId(warehouse.getId());
        location.setWarehouseCode(warehouse.getCode());
        location.setWarehouseName(warehouse.getName());
        location.setCode(code);
        location.setName(request.getName().trim());
        location.setAreaCode(trimToNull(request.getAreaCode()));
        location.setAreaName(trimToNull(request.getAreaName()));
        location.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        location.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        location.setRemark(trimToNull(request.getRemark()));
        location.setUpdatedBy(SecurityUtils.getUserId());
        location.setUpdatedAt(now);
        if (id == null) {
            locationMapper.insert(location);
        } else {
            locationMapper.updateById(location);
        }
        return toLocationVO(location);
    }

    private void syncLocationWarehouseSnapshot(InventoryWarehouse warehouse) {
        List<InventoryLocation> locations = locationMapper.selectList(new LambdaQueryWrapper<InventoryLocation>()
                .eq(InventoryLocation::getWarehouseId, warehouse.getId()));
        for (InventoryLocation location : locations) {
            location.setWarehouseCode(warehouse.getCode());
            location.setWarehouseName(warehouse.getName());
            location.setUpdatedBy(SecurityUtils.getUserId());
            location.setUpdatedAt(OffsetDateTime.now());
            locationMapper.updateById(location);
        }
    }

    private void validateWarehouseRequest(InventoryWarehouseRequest request) {
        if (request == null || !hasText(request.getCode()) || !hasText(request.getName())) {
            throw new BizException(10004, "仓库编码和名称不能为空");
        }
    }

    private void validateLocationRequest(InventoryLocationRequest request) {
        if (request == null || request.getWarehouseId() == null || !hasText(request.getCode()) || !hasText(request.getName())) {
            throw new BizException(10004, "仓库、库位编码和名称不能为空");
        }
    }

    private InventoryWarehouseVO toWarehouseVO(InventoryWarehouse warehouse) {
        InventoryWarehouseVO vo = new InventoryWarehouseVO();
        vo.setId(warehouse.getId());
        vo.setCode(warehouse.getCode());
        vo.setName(warehouse.getName());
        vo.setAddress(warehouse.getAddress());
        vo.setManagerName(warehouse.getManagerName());
        vo.setPhone(warehouse.getPhone());
        vo.setSortOrder(warehouse.getSortOrder());
        vo.setStatus(warehouse.getStatus());
        vo.setRemark(warehouse.getRemark());
        vo.setCreatedAt(warehouse.getCreatedAt());
        vo.setUpdatedAt(warehouse.getUpdatedAt());
        return vo;
    }

    private InventoryLocationVO toLocationVO(InventoryLocation location) {
        InventoryLocationVO vo = new InventoryLocationVO();
        vo.setId(location.getId());
        vo.setWarehouseId(location.getWarehouseId());
        vo.setWarehouseCode(location.getWarehouseCode());
        vo.setWarehouseName(location.getWarehouseName());
        vo.setCode(location.getCode());
        vo.setName(location.getName());
        vo.setAreaCode(location.getAreaCode());
        vo.setAreaName(location.getAreaName());
        vo.setSortOrder(location.getSortOrder());
        vo.setStatus(location.getStatus());
        vo.setRemark(location.getRemark());
        vo.setCreatedAt(location.getCreatedAt());
        vo.setUpdatedAt(location.getUpdatedAt());
        return vo;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
